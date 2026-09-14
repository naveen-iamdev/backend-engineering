package com.naveen.http;

import java.nio.charset.StandardCharsets;

public class HttpResponseBuilder {
    public static byte[] build(HttpResponse response){
        byte[] bodyBytes = response.body().getBytes(StandardCharsets.UTF_8);

        String headers =
                "HTTP/1.1 " +
                response.statusCode() + " " +
                response.statusText() + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "Connection: keep-alive\r\n" +
                "\r\n";
        
        byte[] headerBytes = headers.getBytes(StandardCharsets.UTF_8);

        byte[] result = new byte[headerBytes.length + bodyBytes.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, result, headerBytes.length, bodyBytes.length);

        return result;
    }    
}
