package com.naveen.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
public class HttpRequestParser {
    public static HttpRequest parse(InputStream input) throws IOException {
        // First read the request line + headers.
        // HTTP headers end with \r\n\r\n.
        String headerSection = readHeaders(input);
        if (headerSection == null || headerSection.isEmpty()) {
            return null;
        }
        String[] lines = headerSection.split("\r\n");

        // Example:
        // GET /hello HTTP/1.1
        String[] requestLine = lines[0].split(" ");

        if(requestLine.length != 3) {
            throw new IllegalArgumentException("malformed request line: " + lines[0]);
        }
        String method = requestLine[0];
        String path = requestLine[1];
        String version = requestLine[2];

        // Parse headers
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int seperatorIndex = line.indexOf(":");
            if (seperatorIndex <=0) {
                throw new IllegalArgumentException("malformed header line: " + line);
            }
            
            String name = line.substring(0, seperatorIndex).trim().toLowerCase();
            String value = line.substring(seperatorIndex + 1).trim();
            headers.put(name, value);
        }

        // For now our body framing supports Content-Length.
        int contentLength = 0;
        if (headers.get("content-length") != null) {
            contentLength = Integer.parseInt(headers.get("content-length"));
        }
        byte[] bodyBytes = input.readNBytes(contentLength);
        if (bodyBytes.length != contentLength) {
            throw new IllegalArgumentException("malformed body: expected " + contentLength + " bytes, but got " + bodyBytes.length);
        }

        
        String body =new String(bodyBytes, StandardCharsets.UTF_8);

        return new HttpRequest(method, path, version, headers, body);
    }    

    private static String readHeaders(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        
        int current;

        while ((current = input.read()) != -1) {
            bytes.write(current);
                    
            byte[] data = bytes.toByteArray();
            int length = data.length;

            // HTTP header section ends with:
            //
            // \r\n
            // \r\n
            //
            // This is the boundary between headers and body.
            if (length >= 4 && data[length - 4] == '\r' &&
                data[length - 3] == '\n' &&
                data[length - 2] == '\r' &&
                data[length - 1] == '\n') {
                return bytes.toString(StandardCharsets.UTF_8);
            }
           
        }
        if (bytes.size() == 0) {
            return null;
        }
        throw new IOException("Connection closed before HTTP headers were complete");
    }
}
