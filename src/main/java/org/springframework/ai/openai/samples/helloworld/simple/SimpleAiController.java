package org.springframework.ai.openai.samples.helloworld.simple;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.samples.helloworld.output.AuthorPoems;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@RestController
public class SimpleAiController {

    private final OpenAiChatClient chatClient;

    @Autowired
    public SimpleAiController(OpenAiChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 对话接口
     * @param message
     * @return
     */
    @GetMapping("/ai/simple")
    public Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", chatClient.call(message));
    }


    /**
     * 流式对话接口
     *
     * @param message
     * @return
     */
    @GetMapping("/ai/stream")
    public SseEmitter streamCompletion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        SseEmitter emitter = new SseEmitter(5L * 60 * 1000);
        Flux<String> stream = chatClient.stream(message);
        stream.subscribe(it -> {
            try {
                System.out.println(it);
                emitter.send(it, MediaType.TEXT_EVENT_STREAM);
            } catch (IOException e) {
                System.out.println("sse发送消息失败");
                emitter.completeWithError(e);
            }
        });

        stream.doOnError(e -> {
            System.out.println("流式对话发生异常");
            emitter.completeWithError(e);
        });
        stream.doOnCancel(()->{
            System.out.println("流式对话取消");
            emitter.complete();
        });

        stream.doOnComplete(emitter::complete);
        return emitter;
    }


    /**
     * 模板对话接口，可以用于提示用户，也可以引导chatGpt的回答
     * 可以对chat的回答指定格式，轻松转换为java的实体类
     *
     * @return
     */
    @GetMapping("/ai/template")
    @ResponseBody
    public AuthorPoems templateCompletion(@RequestParam(value = "author", defaultValue = "李白") String author) {
        var outputParser = new BeanOutputParser<>(AuthorPoems.class);
        String message = """
             请列出关于{author}相关的诗词题目
             {format}
             """;

        PromptTemplate promptTemplate = new PromptTemplate(message, Map.of("author", author, "format", outputParser.getFormat()));
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();

        AuthorPoems authorPoems = outputParser.parse(generation.getOutput().getContent());
        return authorPoems;
    }


}
