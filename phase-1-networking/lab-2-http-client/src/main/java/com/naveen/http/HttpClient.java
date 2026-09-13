package com.naveen.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpClient{
    public static void main(String[] args) throws IOException{
        try(Socket socket = new Socket("example.com", 80)){
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            String request = "GET / HTTP/1.1\r\n"+
                    "Host: example.com\r\n"+
                    "Connection: close\r\n"+
                    "\r\n";
            
            output.write(request.getBytes(StandardCharsets.UTF_8));
            output.flush();

            String headers = readHeaders(input);

            System.out.println("----HEADERS----");
            System.out.println(headers);

            if(headers.toLowerCase().contains("transfer-encoding: chunked")){
                byte[] body = readChunkedBody(input);
                System.out.println("----BODY----");
                //System.out.println(Arrays.toString(body));
                System.out.println(new String(body, StandardCharsets.UTF_8));
                
            }else{
                System.out.println("Content-Length header found");

                int contentLength = getContentLength(headers);



                byte[] body = input.readNBytes(contentLength);

                System.out.println("----BODY----");
                System.out.println(Arrays.toString(body));
            }
        }
    }

    public static String readHeaders(InputStream input) throws IOException{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        int previous = -1;
        int current = 0;

        while((current = input.read()) !=-1){
            bytes.write(current);

            if(previous == '\r' && current == '\n'){}

            byte[] data = bytes.toByteArray();
            int length = data.length;

            if(length >=4 && 
                data[length - 4] == '\r' &&
                data[length - 3] == '\n' &&
                data[length - 2] == '\r' &&
                data[length - 1] == '\n'
            ){
                break;
            }
            previous = current;
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
            if(chunkSize == 0){
                readLine(input);
                break;
            }

            byte[] chunk = input.readNBytes(chunkSize);
            if(chunk.length != chunkSize){
                throw new IOException("Unexpected end of stream");
            }
            body.write(chunk);
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