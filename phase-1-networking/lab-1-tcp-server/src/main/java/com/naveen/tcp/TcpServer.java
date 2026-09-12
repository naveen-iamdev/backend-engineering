package com.naveen.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.net.SocketTimeoutException;

public class TcpServer {

    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(9090)) {

            System.out.println("Server listening on port 9090...");

            while (true) {
                Socket socket = serverSocket.accept();

                Thread worker = new Thread(() -> handleClient(socket));
                worker.start();
            }
        }
    }

    private static void handleClient(Socket socket) {

        try (socket) {

            System.out.println(
                    "Client connected: " + socket.getRemoteSocketAddress()
            );

            // A client can stay connected, but we don't want a worker
            // waiting forever if the client stops sending data.
            socket.setSoTimeout(10_000);

            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            byte[] buffer = new byte[1024];

            while (true) {

                int count;

                try {
                    count = input.read(buffer);
                } catch (SocketTimeoutException e) {
                    System.out.println("Client inactive for 10 seconds");
                    break;
                }

                if (count == -1) {
                    System.out.println("Client disconnected");
                    break;
                }

                String message =
                        new String(buffer, 0, count, StandardCharsets.UTF_8);

                System.out.println("Received: " + message);

                String response = "Server received: " + message;
                output.write(response.getBytes(StandardCharsets.UTF_8));
                output.flush();
            }

        } catch (IOException e) {
            System.out.println(
                    "Client communication failed: " + e.getMessage()
            );
        }
    }
}
