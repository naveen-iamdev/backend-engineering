package com.naveen.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class HttpServer {
    public static void main(String[] args) throws IOException {
        // Create a server socket that listens on port 9090
        try (ServerSocket serverSocket = new ServerSocket(9090)){
            System.out.println("Server is listening on port 9090");

            while(true){
                // Accept a new client connection
                try(Socket socket = serverSocket.accept()){
                    System.out.println("Client connected: " + socket.getRemoteSocketAddress());

                    InputStream input = socket.getInputStream();
                    OutputStream output = socket.getOutputStream();

                    while (true) {
                        
                        HttpRequest request;
                        try {
                            request = HttpRequestParser.parse(input);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error parsing request: " + e.getMessage());
                            HttpResponse errorResponse = new HttpResponse(
                                    400,
                                    "Bad Request",
                                    "Malformed request"
                            );
                            byte[] responseBytes = HttpResponseBuilder.build(errorResponse);
                            output.write(responseBytes);
                            output.flush();
                            break;
                        }
                        request = HttpRequestParser.parse(input);
                        if (request == null) {
                            System.out.println("Client closed connection.");
                            break;
                        }
                        System.out.println("Method: " + request.method());
                        System.out.println("Path: " + request.path());
                        System.out.println("Version: " + request.version());
                        System.out.println("Headers: " + request.headers());
                        System.out.println("Body: " + request.body());

                        HttpResponse responseBody = HttpRouter.route(request);
                        byte[] responseBytes = HttpResponseBuilder.build(responseBody);
                        output.write(responseBytes);
                        
                        output.flush();
                        System.out.println("Response sent to client.");

                        if("close".equalsIgnoreCase(request.headers().get("connection"))) { 
                            System.out.println("Connection closed by client.");
                            break;
                        }
                    }
                    
                    
                }
            }
        }
    }
}