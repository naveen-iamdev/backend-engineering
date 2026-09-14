package com.naveen.http;

public class HttpRouter {

    public static HttpResponse route(HttpRequest request) {

        if (request.method().equals("GET") &&
                request.path().equals("/hello")) {

            return new HttpResponse(
                    200,
                    "OK",
                    "Hello, World!"
            );
        }

        if (request.method().equals("GET") &&
                request.path().equals("/")) {

            return new HttpResponse(
                    200,
                    "OK",
                    "Welcome to my HTTP server!"
            );
        }

        if (request.method().equals("POST") &&
            request.path().equals("/echo")) {

            return new HttpResponse(
                    200,
                    "OK",
                    request.body()
            );
        }       

        return new HttpResponse(
                404,
                "Not Found",
                "Resource not found"
        );
    }
}