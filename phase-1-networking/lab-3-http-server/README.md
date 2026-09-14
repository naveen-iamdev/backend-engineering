# Lab 3 — Tiny HTTP Server

## Purpose

Build a small HTTP server directly on top of Java TCP sockets.

```text
HTTP client
    ↓
TCP Socket
    ↓
HTTP parser
    ↓
routing
    ↓
HTTP response
```

## What it teaches

* HTTP request parsing
* Method/path routing
* Response construction
* Request bodies with `Content-Length`
* HTTP headers
* Keep-alive
* `400` / `404` handling

## Request parsing

```http
POST /echo HTTP/1.1
Host: localhost:9090
Content-Type: text/plain
Content-Length: 12

hello server
```

Parsed into:

```text
method  → POST
path    → /echo
version → HTTP/1.1
headers → Map
body    → hello server
```

Headers end at:

```text
\r\n\r\n
```

`Content-Length` tells the server how many body bytes to read.

## Routing

```text
GET  /      → 200
GET  /hello → 200
POST /echo  → 200
other       → 404
```

## Response

```http
HTTP/1.1 200 OK
Content-Type: text/plain
Content-Length: 13
Connection: keep-alive

Hello, World!
```

Response structure:

```text
status line
headers
blank line
body
```

## Keep-alive

Multiple HTTP requests can use one TCP connection:

```text
TCP connection
 → request → response
 → request → response
 → close
```

`Connection: close` tells the server/client to close after the response.

## Errors

```text
400 → malformed HTTP request
404 → route/resource not found
```

A bad request should produce an HTTP response rather than crash the server.

## Test

```bash
./mvnw clean compile
java -cp target/classes com.naveen.http.HttpServer
```

```bash
curl -i http://127.0.0.1:9090/hello
curl -i http://127.0.0.1:9090/does-not-exist
curl -i -X POST -H "Content-Type: text/plain" -d "hello server" http://127.0.0.1:9090/echo
```

Keep-alive test:

```bash
printf 'GET /hello HTTP/1.1\r\nHost: 127.0.0.1:9090\r\nConnection: keep-alive\r\n\r\nGET / HTTP/1.1\r\nHost: 127.0.0.1:9090\r\nConnection: close\r\n\r\n' | nc 127.0.0.1 9090
```

## Key takeaway

```text
TCP = byte stream
HTTP = structure/rules for those bytes
Server = parse → route → respond
```
