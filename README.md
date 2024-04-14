# 什么是spring-ai
Spring AI 是一个与 Spring 生态系统紧密集成的项目，旨在简化在基于 Spring 的应用程序中使用人工智能（AI）技术的过程。
简化集成：Spring AI 为开发者提供了方便的工具和接口，使得在 Spring 应用中集成 AI 功能变得更加容易，**避免了手动处理底层细节的复杂性。**
# spring-ai具备什么功能
下面是spring官方文档介绍
Spring AI API 涵盖了广泛的功能。 每个主要功能都在其单独的部分中进行了详细说明。 为了提供概述，提供了以下关键功能：

- 跨 AI 提供商的可移植 API，用于聊天、文本到图像和嵌入模型。支持同步和流 API 选项。还支持下拉以访问特定于模型的功能。我们支持来自OpenAI，Microsoft，Amazon，Google，Huggingface等的AI模型。
- 跨 Vector Store 提供程序的可移植 API，包括新颖的类似 SQL 的元数据过滤器 API，该 API 也是可移植的。支持 8 个向量数据库。
- 函数调用。Spring AI 可以轻松地让 AI 模型调用您的 POJO java.util.Function 对象。
- AI 模型和矢量存储的 Spring Boot 自动配置和启动器。
- 用于数据工程的 ETL 框架。这为将数据加载到向量数据库提供了基础，有助于实现检索增强生成模式，使您能够将数据引入 AI 模型以合并到其响应中。

**总结来说。通过spring-ai的提供的api功能，可以方便的实现大模型相关的相关功能。例如对话，提示，文生图等功能。**

![image.png](https://cdn.nlark.com/yuque/0/2024/png/23165007/1713066556770-3a15e9c9-a85f-4eed-9e01-73a03c4373b2.png#averageHue=%231e2328&clientId=ua1cef76a-d5af-4&from=paste&height=390&id=v6UvU&originHeight=585&originWidth=427&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=36254&status=done&style=none&taskId=ueb5cc8f9-f0bf-45b7-8130-08ac794ff22&title=&width=284.6666666666667)
# 接入chatGpt
spring-ai官方提供了一个demo
[https://github.com/rd-1-2022/ai-openai-helloworld](https://github.com/rd-1-2022/ai-openai-helloworld)
这个demo中，可以通过简单的配置实现会话功能，但是没有提供设置代理的功能，国内使用的话会请求超市，无法访问社区也有相关的反馈
![image.png](https://cdn.nlark.com/yuque/0/2024/png/23165007/1713067533676-2c9ede72-9095-4b83-8942-94acee9d5987.png#averageHue=%23b0a395&clientId=ua1cef76a-d5af-4&from=paste&height=839&id=nkWjR&originHeight=1258&originWidth=1524&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=386851&status=done&style=none&taskId=u7655dd91-06c5-44af-8b0b-1d16c536216&title=&width=1016)
因此我稍微改写了这个demo，提供了设置代理的功能。完善了相关配置
# 项目介绍
在ai-openai-helloworld基础上进行了一些修改，完善可一些配置文件，增加了代理的配置。国内可以通过配置代理访问openAI。
实现了3个demo
1、简单回话（全部生成完毕返回）
```
curl --location 'http://localhost:8080/ai/simple?message=Tell me a joke'
```
返回完整的生成回话
![image.png](https://cdn.nlark.com/yuque/0/2024/png/23165007/1713079698050-4f1412cf-cb02-44eb-b3a0-ca864d09913b.png#averageHue=%23fbfbfb&clientId=ua1cef76a-d5af-4&from=paste&height=288&id=xlI7v&originHeight=432&originWidth=1381&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=54946&status=done&style=none&taskId=uc7226160-aab6-408a-8993-e77f7695ecd&title=&width=920.6666666666666)
2、流式回话
```
curl --location 'http://localhost:8080/ai/stream?message=Tell me a joke'
```
![image.png](https://cdn.nlark.com/yuque/0/2024/png/23165007/1713080082882-5f92c5aa-9d61-4bac-a029-339a62fbd8a1.png#averageHue=%23fdfdfd&clientId=ua1cef76a-d5af-4&from=paste&height=758&id=tkhor&originHeight=1137&originWidth=1921&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=113381&status=done&style=none&taskId=u893ca74d-09ce-4112-a832-ab9f87fb1bc&title=&width=1280.6666666666667)
3、消息的模板提示和对gpt相应的结果自动映射到java类
```
curl --location 'http://localhost:8080/ai/template?author=李白'
```
例如这个例子中我们像gpt提问请列出关于**李白**相关的诗词题目，spring-ai可以自动帮我们解析成java对象
![image.png](https://cdn.nlark.com/yuque/0/2024/png/23165007/1713080211141-0946cf32-83e2-49af-8382-52e348a1fefc.png#averageHue=%23fdfdfd&clientId=ua1cef76a-d5af-4&from=paste&height=684&id=F768L&originHeight=1026&originWidth=1372&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=115913&status=done&style=none&taskId=ud2dd4573-dd49-440d-8945-b95e783f70d&title=&width=914.6666666666666)
# 启动项目
需要修改application.yml文件
填写正确的apiKey。与代理服务器的配置
```
spring:
  ai:
    proxy:
      host: 127.0.0.1
      port: 1080
    openai:
      api-key: xxxx
      chat:
        options:
          model: gpt-3.5-turbo


```
# 接口说明
```java
  /**
     * 对话接口,回话生成完毕返回
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
```
# 项目结构
整个项目十分简单，代码很少。主要为了实现设置代理类重写了了openai一些接口
![image.png](https://cdn.nlark.com/yuque/0/2024/png/23165007/1713081561428-c7e6f832-5b6c-49ba-b8bb-5b8317e6f2ed.png#averageHue=%232c2f33&clientId=ua1cef76a-d5af-4&from=paste&height=362&id=ubb1f6106&originHeight=543&originWidth=676&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=61160&status=done&style=none&taskId=uf7fac319-5f4a-4228-a47f-a6e04d30e7b&title=&width=450.6666666666667)
