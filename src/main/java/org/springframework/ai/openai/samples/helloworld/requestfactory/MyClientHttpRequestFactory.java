package org.springframework.ai.openai.samples.helloworld.requestfactory;


import java.io.IOException;
import java.net.*;
import java.time.Duration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class MyClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    public MyClientHttpRequestFactory(Proxy proxy) {
        super();
        super.setProxy(proxy);
    }

    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return super.createRequest(uri,httpMethod);
    }

}
