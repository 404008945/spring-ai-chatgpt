package org.springframework.ai.openai.samples.helloworld.openapi.requestfactory;


import java.io.IOException;
import java.net.*;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

public class MyClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    public MyClientHttpRequestFactory(Proxy proxy) {
        super();
        super.setProxy(proxy);
    }

    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return super.createRequest(uri,httpMethod);
    }

}
