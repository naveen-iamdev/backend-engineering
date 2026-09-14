package com.naveen.http;

public record HttpResponse(
        int statusCode,
        String statusText,
        String body
) {
}
