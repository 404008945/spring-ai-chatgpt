package org.springframework.ai.openai.samples.helloworld.openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiStreamFunctionCallingHelper;
import org.springframework.ai.openai.samples.helloworld.config.ProxyConfig;
import org.springframework.ai.openai.samples.helloworld.requestfactory.MyClientHttpRequestFactory;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

/**
 * OpenAiApi这个类设计上没有考虑到继承扩展
 * 但是这里需要实现设置代理类，因此还是继承了，写的都是大量重复代码，实现上不是很优雅
 */
public class MyOpenAiApi extends OpenAiApi {
    private static final Predicate<String> SSE_DONE_PREDICATE;
    private final RestClient restClient;
    private final WebClient webClient;
    private OpenAiStreamFunctionCallingHelper chunkMerger;

    public MyOpenAiApi(String openAiToken) {
        this("https://api.openai.com", openAiToken);
    }

    public MyOpenAiApi(String baseUrl, String openAiToken) {
        this(baseUrl, openAiToken, RestClient.builder());
    }

    public MyOpenAiApi(String baseUrl, String openAiToken, RestClient.Builder restClientBuilder) {
        this(null,baseUrl, openAiToken, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
    }

    public MyOpenAiApi(ProxyConfig proxyConfig, String baseUrl, String openAiToken, RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {
        super(openAiToken);
        this.chunkMerger = new OpenAiStreamFunctionCallingHelper();
        WebClient.Builder webBuilder = WebClient.builder();
        if(StringUtils.isNotBlank(proxyConfig.getHost())&& ObjectUtils.isNotEmpty(proxyConfig.getPort())) {
            restClientBuilder.requestFactory(new MyClientHttpRequestFactory(new Proxy(Proxy.Type.HTTP,new InetSocketAddress(proxyConfig.getHost(),proxyConfig.getPort()))));
            HttpClient httpClient = HttpClient.create()
                    .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                            .host(proxyConfig.getHost())
                            .port(proxyConfig.getPort()));
            // 使用配置好的 HttpClient 创建 ClientHttpConnector
            ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
            webBuilder.clientConnector(connector);
        }

        this.restClient = restClientBuilder.baseUrl(baseUrl).defaultHeaders(ApiUtils.getJsonContentHeaders(openAiToken)).defaultStatusHandler(responseErrorHandler).build();
        this.webClient = webBuilder.baseUrl(baseUrl).defaultHeaders(ApiUtils.getJsonContentHeaders(openAiToken)).build();
    }

    public ResponseEntity<org.springframework.ai.openai.api.OpenAiApi.ChatCompletion> chatCompletionEntity(org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(!chatRequest.stream(), "Request must set the steam property to false.");
        return ((RestClient.RequestBodySpec)this.restClient.post().uri("/v1/chat/completions", new Object[0])).body(chatRequest).retrieve().toEntity(org.springframework.ai.openai.api.OpenAiApi.ChatCompletion.class);
    }

    public Flux<org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk> chatCompletionStream(org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream(), "Request must set the steam property to true.");
        AtomicBoolean isInsideTool = new AtomicBoolean(false);
        return ((WebClient.RequestBodySpec)this.webClient.post().uri("/v1/chat/completions", new Object[0])).body(Mono.just(chatRequest), org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.class).retrieve().bodyToFlux(String.class).takeUntil(SSE_DONE_PREDICATE).filter(SSE_DONE_PREDICATE.negate()).map((content) -> {
            return (org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk)ModelOptionsUtils.jsonToObject(content, org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk.class);
        }).map((chunk) -> {
            if (this.chunkMerger.isStreamingToolFunctionCall(chunk)) {
                isInsideTool.set(true);
            }

            return chunk;
        }).windowUntil((chunk) -> {
            if (isInsideTool.get() && this.chunkMerger.isStreamingToolFunctionCallFinish(chunk)) {
                isInsideTool.set(false);
                return true;
            } else {
                return !isInsideTool.get();
            }
        }).concatMapIterable((window) -> {
            Mono<org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk> monoChunk = window.reduce(new org.springframework.ai.openai.api.OpenAiApi.ChatCompletionChunk((String)null, (List)null, (Long)null, (String)null, (String)null, (String)null), (previous, current) -> {
                return this.chunkMerger.merge(previous, current);
            });
            return List.of(monoChunk);
        }).flatMap((mono) -> {
            return mono;
        });
    }

    public <T> ResponseEntity<org.springframework.ai.openai.api.OpenAiApi.EmbeddingList<org.springframework.ai.openai.api.OpenAiApi.Embedding>> embeddings(org.springframework.ai.openai.api.OpenAiApi.EmbeddingRequest<T> embeddingRequest) {
        Assert.notNull(embeddingRequest, "The request body can not be null.");
        Assert.notNull(embeddingRequest.input(), "The input can not be null.");
        Assert.isTrue(embeddingRequest.input() instanceof String || embeddingRequest.input() instanceof List, "The input must be either a String, or a List of Strings or List of List of integers.");
        Object var3 = embeddingRequest.input();
        if (var3 instanceof List list) {
            Assert.isTrue(!CollectionUtils.isEmpty(list), "The input list can not be empty.");
            Assert.isTrue(list.size() <= 2048, "The list must be 2048 dimensions or less");
            Assert.isTrue(list.get(0) instanceof String || list.get(0) instanceof Integer || list.get(0) instanceof List, "The input must be either a String, or a List of Strings or list of list of integers.");
        }

        return ((RestClient.RequestBodySpec)this.restClient.post().uri("/v1/embeddings", new Object[0])).body(embeddingRequest).retrieve().toEntity(new ParameterizedTypeReference<org.springframework.ai.openai.api.OpenAiApi.EmbeddingList<org.springframework.ai.openai.api.OpenAiApi.Embedding>>() {
        });
    }

    static {
        SSE_DONE_PREDICATE = "[DONE]"::equals;
    }
}
