package com.naveen.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpClient{
    public static void main(String[] args) throws IOException{
        // URI helps us break a URL into its useful pieces:
        // host, port, path and query.
        URI uri = URI.create("http://example.com/search?page=2&sort=name");

        String host = uri.getHost();

        // If the URL does not explicitly contain a port,
        // use HTTP's default port 80.
        int port = uri.getPort() == -1 ? 80 : uri.getPort();

        String path = uri.getPath();
        if(path == null || path.isEmpty()){
            path = "/";
        }

        // Query parameters are part of the HTTP request target.
        String query = uri.getQuery();
        if(query != null && !query.isEmpty()){
            path += "?"+query;
        }

        System.out.println("Host: "+host);
        System.out.println("Port: "+port);
        System.out.println("Path: "+path);
        System.out.println("Query: "+query);
        System.out.println("full URL: "+uri.toString());


        // This Socket is the TCP connection underneath HTTP.
        try(Socket socket = new Socket(host, port)){
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            // Request 1.
            // We keep the TCP connection open so another HTTP request
            // can reuse the same connection.
            sendRequest(output,path);
            readResponse(input);

            // Request 2.
            // No new Socket is created here.
            // This is the same TCP connection as request 1.
            sendRequest(output,"/");
            readResponse(input);
        }
    }

    public static void sendRequest(OutputStream output, String path) throws IOException{

        // HTTP is just a defined format for bytes travelling over TCP.
        //
        // \r\n marks the end of each HTTP line.
        // The final \r\n creates the blank line that marks the
        // end of the headers.
        String request = "GET "+path+" HTTP/1.1\r\n"+
                    "Host: example.com\r\n"+
                    "Connection: keep-alive\r\n"+
                    "\r\n";
            
        System.out.println("---- REQUEST ----");
        System.out.print(request);

        // A Java String must be converted to bytes before
        // it can be sent through the network socket.
        output.write(request.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }
    
    public static void readResponse(InputStream input) throws IOException{
            // Read until \r\n\r\n, which marks the end of HTTP headers.
            String headers = readHeaders(input);

            System.out.println("----HEADERS----");
            System.out.println(headers);

            byte[] body;

            // HTTP can tell us how the body is framed in different ways.
            if(headers.toLowerCase().contains("transfer-encoding: chunked")){
                
                // Read chunks until a zero-size chunk indicates
                // that the response body is complete.
                body = readChunkedBody(input);
                
            }else{
                // Content-Length tells us exactly how many bytes
                // belong to the response body.
                int contentLength = getContentLength(headers);

                body = input.readNBytes(contentLength);

                 if (body.length != contentLength) {
                    throw new IOException("Unexpected end of response");
                }
            }

            System.out.println("----BODY----");
            //System.out.println(Arrays.toString(body));

            // This example returns HTML, so we interpret the bytes as UTF-8 text.
            System.out.println(new String(body, StandardCharsets.UTF_8));
            
        
    }

    public static String readHeaders(InputStream input) throws IOException{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        int current ;

        while((current = input.read()) !=-1){
            bytes.write(current);

            byte[] data = bytes.toByteArray();
            int length = data.length;

            // HTTP headers end with:
            //
            // \r\n
            // \r\n
            //
            // In other words, CRLF followed by another CRLF.

            if(length >=4 && 
                data[length - 4] == '\r' &&
                data[length - 3] == '\n' &&
                data[length - 2] == '\r' &&
                data[length - 1] == '\n'
            ){
                break;
            }
        }

         if (current == -1) {
            throw new IOException("Response ended before headers were complete");
        }

        return bytes.toString(StandardCharsets.UTF_8);
    }

    public static int getContentLength(String headers){
        for(String line: headers.split("\r\n")){
            if(line.toLowerCase().startsWith("content-length:")){
                return Integer.parseInt(line.substring("content-length:".length()).trim());
            }
        }

        throw new IllegalStateException("Content-Length header not found");
    }

    public static byte[] readChunkedBody(InputStream input) throws IOException{
        ByteArrayOutputStream body = new ByteArrayOutputStream();

        while(true){
            String sizeLine = readLine(input);
            int chunkSize =Integer.parseInt(sizeLine.trim(), 16);

            // A zero-size chunk means the response body is complete.
            if(chunkSize == 0){
                readLine(input); // Consume the final CRLF.
                break;
            }

            // Read exactly the number of bytes specified by the chunk size.
            byte[] chunk = input.readNBytes(chunkSize);

            if(chunk.length != chunkSize){
                throw new IOException("Unexpected end of stream");
            }

            body.write(chunk);

            // Each chunk's data is followed by CRLF.
            readLine(input);
        }

        return body.toByteArray();
    }

    public static String readLine(InputStream input)throws IOException{
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        
        int current ;

        while((current = input.read()) != -1){
            if(current == '\n'){
                break;
            }

            // Don't include CR in the returned line.
            if(current != '\r'){
                line.write(current);
            }
        }

        if(current==-1){
            throw new IOException("Unexpected end of stream");
        }

        return line.toString(StandardCharsets.UTF_8);
    }
}