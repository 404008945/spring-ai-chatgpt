package org.springframework.ai.openai.samples.helloworld.openapi;

import org.springframework.ai.autoconfigure.openai.*;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.openai.OpenAiAudioTranscriptionClient;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiEmbeddingClient;
import org.springframework.ai.openai.OpenAiImageClient;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.openai.samples.helloworld.openapi.config.ProxyConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
@AutoConfiguration(
        after = {RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class}
)
@EnableConfigurationProperties({OpenAiConnectionProperties.class, OpenAiChatProperties.class, OpenAiEmbeddingProperties.class, OpenAiImageProperties.class, OpenAiAudioTranscriptionProperties.class})
public class MyOpenAiAutoConfiguration {
    public MyOpenAiAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "spring.ai.openai.chat",
            name = {"enabled"},
            havingValue = "true",
            matchIfMissing = true
    )
    public OpenAiChatClient openAiChatClient(ProxyConfig proxyConfig, OpenAiConnectionProperties commonProperties, OpenAiChatProperties chatProperties, RestClient.Builder restClientBuilder, List<FunctionCallback> toolFunctionCallbacks, FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler) {
        MyOpenAiApi openAiApi = this.openAiApi(proxyConfig,chatProperties.getBaseUrl(), commonProperties.getBaseUrl(), chatProperties.getApiKey(), commonProperties.getApiKey(), restClientBuilder, responseErrorHandler);
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }

        return new OpenAiChatClient(openAiApi, chatProperties.getOptions(), functionCallbackContext, retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "spring.ai.openai.embedding",
            name = {"enabled"},
            havingValue = "true",
            matchIfMissing = true
    )
    public OpenAiEmbeddingClient openAiEmbeddingClient(ProxyConfig proxyConfig,OpenAiConnectionProperties commonProperties, OpenAiEmbeddingProperties embeddingProperties, RestClient.Builder restClientBuilder, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler) {
        MyOpenAiApi openAiApi = this.openAiApi(proxyConfig,embeddingProperties.getBaseUrl(), commonProperties.getBaseUrl(), embeddingProperties.getApiKey(), commonProperties.getApiKey(), restClientBuilder, responseErrorHandler);
        return new OpenAiEmbeddingClient(openAiApi, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions(), retryTemplate);
    }

    private MyOpenAiApi openAiApi(ProxyConfig proxyConfig,String baseUrl, String commonBaseUrl, String apiKey, String commonApiKey, RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {
        String resolvedBaseUrl = StringUtils.hasText(baseUrl) ? baseUrl : commonBaseUrl;
        Assert.hasText(resolvedBaseUrl, "OpenAI base URL must be set");
        String resolvedApiKey = StringUtils.hasText(apiKey) ? apiKey : commonApiKey;
        Assert.hasText(resolvedApiKey, "OpenAI API key must be set");
        return new MyOpenAiApi(proxyConfig,resolvedBaseUrl, resolvedApiKey, restClientBuilder, responseErrorHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "spring.ai.openai.image",
            name = {"enabled"},
            havingValue = "true",
            matchIfMissing = true
    )
    public OpenAiImageClient openAiImageClient(OpenAiConnectionProperties commonProperties, OpenAiImageProperties imageProperties, RestClient.Builder restClientBuilder, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler) {
        String apiKey = StringUtils.hasText(imageProperties.getApiKey()) ? imageProperties.getApiKey() : commonProperties.getApiKey();
        String baseUrl = StringUtils.hasText(imageProperties.getBaseUrl()) ? imageProperties.getBaseUrl() : commonProperties.getBaseUrl();
        Assert.hasText(apiKey, "OpenAI API key must be set");
        Assert.hasText(baseUrl, "OpenAI base URL must be set");
        OpenAiImageApi openAiImageApi = new OpenAiImageApi(baseUrl, apiKey, restClientBuilder, responseErrorHandler);
        return new OpenAiImageClient(openAiImageApi, imageProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAiAudioTranscriptionClient openAiAudioTranscriptionClient(OpenAiConnectionProperties commonProperties, OpenAiAudioTranscriptionProperties transcriptionProperties, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler) {
        String apiKey = StringUtils.hasText(transcriptionProperties.getApiKey()) ? transcriptionProperties.getApiKey() : commonProperties.getApiKey();
        String baseUrl = StringUtils.hasText(transcriptionProperties.getBaseUrl()) ? transcriptionProperties.getBaseUrl() : commonProperties.getBaseUrl();
        Assert.hasText(apiKey, "OpenAI API key must be set");
        Assert.hasText(baseUrl, "OpenAI base URL must be set");
        OpenAiAudioApi openAiAudioApi = new OpenAiAudioApi(baseUrl, apiKey, RestClient.builder(), responseErrorHandler);
        OpenAiAudioTranscriptionClient openAiChatClient = new OpenAiAudioTranscriptionClient(openAiAudioApi, transcriptionProperties.getOptions(), retryTemplate);
        return openAiChatClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }
}
