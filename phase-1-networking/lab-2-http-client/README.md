# Lab 2 — HTTP Client

## Purpose

Build an HTTP client directly using Java `Socket` to understand:

```text
HTTP → TCP → Socket → OS
```

HTTP gives structure/meaning to the bytes carried by TCP.

## Key concepts

### HTTP request

```http
GET /search?page=2&sort=name HTTP/1.1
Host: example.com
Connection: keep-alive

```

* `GET` → method
* `/search` → path/resource
* `?page=2&sort=name` → query parameters
* `HTTP/1.1` → HTTP version
* headers → extra request information
* blank line → headers end
* body → optional data

### URL

```text
http://example.com/search?page=2
```

```text
http://     → scheme
example.com → host
/search     → path
?page=2     → query
```

`URI.getPort()` returns `-1` when the URL has no explicit port, so HTTP uses the default:

```text
80 → HTTP
443 → HTTPS
```

### HTTP response

```http
HTTP/1.1 200 OK
Content-Type: text/html
Content-Length: 10

<10 bytes>
```

Common codes:

```text
2xx → success
3xx → redirect
4xx → client/request problem
5xx → server problem

200 → OK
201 → Created
204 → No Content
400 → Bad Request
401 → Unauthorized
403 → Forbidden
404 → Not Found
409 → Conflict
500 → Server error
```

Important:

```text
Connection refused
→ TCP connection could not be established

404
→ TCP connection succeeded, HTTP worked,
  but the requested resource wasn't found
```

### Body

Can contain:

```text
JSON / HTML / text / binary
```

`Content-Type` tells the receiver how to interpret it.

### TCP vs HTTP

```text
TCP → byte stream
HTTP → rules for those bytes
```

TCP does not know what `GET`, headers or JSON mean.

### Response framing

HTTP needs to tell the client where the body ends.

```text
Content-Length
→ read exactly N bytes

Transfer-Encoding: chunked
→ read chunk-size (hex) + chunk data
→ repeat until 0

Connection: close
→ body ends when TCP connection closes
```

### Keep-Alive

One TCP connection can carry multiple HTTP requests:

```text
TCP connection
 → request
 → response
 → request
 → response
 → close
```

Therefore:

```text
HTTP response finished ≠ TCP connection closed
```

### HTTP versions

```text
HTTP/1.0 → basic request/response, connections commonly closed
HTTP/1.1 → keep-alive, Host, chunked encoding
HTTP/2   → binary + multiplexed, still commonly over TCP
HTTP/3   → HTTP over QUIC/UDP
```

## Implementation

Our client uses:

```text
URI
Socket
InputStream
OutputStream
```

Flow:

```text
URL
 ↓
parse host/port/path/query
 ↓
TCP Socket
 ↓
HTTP request bytes
 ↓
HTTP server
 ↓
response bytes
 ↓
parse headers/body
```

## Run

```bash
./mvnw clean compile
java -cp target/classes com.naveen.http.HttpClient
```

## Useful tools

Manual HTTP over TCP:

```bash
nc example.com 80
```

Inspect connections:

```bash
ss -tnp
```

Find process using a port:

```bash
lsof -i :80
```

Inspect packets:

```bash
tcpdump -i lo -nn 'tcp port 9090'
```

## Main takeaway

```text
URL
 ↓
HTTP
 ↓
TCP
 ↓
Socket
 ↓
Operating system
```

**HTTP is an application protocol built on top of a byte stream provided by TCP.**

## Next

**Lab 3 — Tiny HTTP Server**
