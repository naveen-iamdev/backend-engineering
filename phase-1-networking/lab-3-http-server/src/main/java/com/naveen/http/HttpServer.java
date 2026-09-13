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

                    // For now, just read everything available from
                    // the client and print it.
                    byte[] buffer = new byte[1024];
                    int bytesRead = input.read(buffer);

                    if(bytesRead != -1){
                        String request = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                        System.out.println("Received request:\n" + request);

                        // Prepare a simple HTTP response
                        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 13\r\n" +
                                "Connection: close\r\n" +
                                "\r\n" +
                                "Hello, World!";

                        // Send the HTTP response to the client
                        output.write(httpResponse.getBytes(StandardCharsets.UTF_8));
                        output.flush();

                        System.out.println("Response sent to client.");
                    }
                }
            }
        }
    }
}