# Lab 1 — TCP Socket Client/Server

Part of my Java Backend Engineering learning track.

## Purpose

Build a small TCP client/server directly in Java before using HTTP, Tomcat, and Spring Boot.

The goal is to understand what happens underneath a backend server when two programs communicate over a network.

---

## What this lab teaches

### IP address

Identifies **which machine/interface** we want to communicate with.

```text
127.0.0.1
```

means "this machine".

Think:

```text
IP address = hotel's street address
```

---

### Port

Identifies **which service/application** on that machine should receive the connection.

```text
127.0.0.1:9090
```

means:

```text
127.0.0.1 → machine
9090      → service listening there
```

Think:

```text
IP   = hotel address
Port = particular entrance/service
```

Examples:

```text
22  → SSH
80  → HTTP
443 → HTTPS
```

---

### TCP

Provides a reliable, ordered **stream of bytes** between two endpoints.

Basic lifecycle:

```text
connect
  ↓
exchange bytes
  ↓
close
```

TCP itself doesn't understand HTTP, JSON, `GET`, or `/users`.

---

### Socket

The program's way of communicating through a network connection.

Java provides:

```java
Socket
```

for an established connection.

Think:

```text
Socket = the handle/communication point for one guest conversation
```

The OS manages the underlying network resource; Java gives our application an API to use it.

---

### `ServerSocket`

Used by a server to create a **listening TCP endpoint**.

```java
ServerSocket serverSocket = new ServerSocket(9090);
```

Conceptually:

```text
Java
 ↓
"Listen for TCP connections on port 9090"
 ↓
Operating system
 ↓
port 9090 is listening
```

Think:

```text
ServerSocket = hotel's front desk waiting for guests
```

---

### `accept()`

Waits for the next client to connect.

```java
Socket socket = serverSocket.accept();
```

It is a **blocking** operation.

No client:

```text
accept()
 ↓
wait...
```

Client arrives:

```text
accept()
 ↓
returns Socket
```

The `Socket` represents that particular client connection.

---

### `InputStream`

Used to read bytes **from the connection**.

```java
InputStream input = socket.getInputStream();
```

```text
client
  ↓
TCP
  ↓
Socket
  ↓
InputStream
  ↓
Java application
```

`read()` may wait if no data is currently available.

---

### `OutputStream`

Used to send bytes **through the connection**.

```java
OutputStream output = socket.getOutputStream();
```

```text
Java application
  ↓
OutputStream
  ↓
Socket
  ↓
TCP
  ↓
client
```

`flush()` is used when we want buffered output pushed onward.

---

### TCP is a byte stream

TCP does **not** preserve application messages.

If the client sends:

```text
HELLO
```

TCP carries bytes:

```text
H E L L O
```

It doesn't know that `HELLO` is one message.

The application/protocol needs rules to decide where a message ends, such as:

```text
delimiter
fixed length
length field
connection close
```

This becomes important in the HTTP lab.

---

### Blocking I/O

A call such as:

```java
input.read(...)
```

may wait until:

* data arrives
* the connection ends
* a timeout occurs
* an error happens

Important distinction:

```text
waiting for data ≠ disconnected
```

---

### EOF

When the other side has finished sending and there is no more data:

```java
read() == -1
```

means:

> The stream has ended.

It does not mean the client sent the number `-1`.

---

### Socket timeout

```java
socket.setSoTimeout(10_000);
```

limits how long a blocking read can wait for data.

This prevents a worker from waiting forever for an inactive client.

```text
data arrives
→ read returns

nothing for 10 seconds
→ timeout exception
```

A timeout is different from EOF.

---

### Multiple clients

A server cannot spend all its time handling one client if other clients are arriving.

We therefore used:

```text
Server
  ├── Worker → Client A
  ├── Worker → Client B
  └── Worker → Client C
```

with **one worker thread per connection**.

This is useful for understanding concurrency, although creating one thread per connection has limits at large scale.

---

## Server flow

```text
Start server
     ↓
Create ServerSocket on :9090
     ↓
accept()
     ↓
Client connects
     ↓
Socket created
     ↓
Worker handles client
     ↓
Read bytes
     ↓
Process
     ↓
Write response
     ↓
Close connection
     ↓
Accept another client
```

---

# OS / debugging tools

These tools let us see what is happening **outside the Java code**.

### `ps`

Shows running processes.

```bash
ps -ef | grep '[T]cpServer'
```

Question answered:

> Is my Java server process running?

---

### `ss`

Shows listening sockets and TCP connections.

Listening:

```bash
ss -lnt | grep 9090
```

Active connections:

```bash
ss -tnp | grep 9090
```

Question answered:

> What network connections/ports does the OS currently know about?

---

### `lsof`

Shows which process has a resource open.

```bash
lsof -i :9090
```

Question answered:

> Which process is using port 9090?

Useful when debugging:

```text
Address already in use
```

---

### `tcpdump`

Shows actual network packets.

```bash
tcpdump -i lo -nn 'tcp port 9090'
```

For our localhost test, `lo` is the loopback interface.

We observed:

```text
SYN
SYN-ACK
ACK
↓
data
↓
connection close
```

This connected our Java code to the actual TCP traffic.

---

### `nc` / netcat

A simple command-line TCP client.

```bash
nc 127.0.0.1 9090
```

Useful for testing the server without writing another Java client.

---

# Useful commands

Build:

```bash
./mvnw clean compile
```

Run:

```bash
java -cp target/classes com.naveen.tcp.TcpServer
```

Connect:

```bash
nc 127.0.0.1 9090
```

Check listening port:

```bash
ss -lnt | grep 9090
```

Check connections:

```bash
ss -tnp | grep 9090
```

Find process using port:

```bash
lsof -i :9090
```

Inspect process:

```bash
ps -ef | grep '[T]cpServer'
```

Inspect threads:

```bash
ps -eLf | grep '[T]cpServer'
```

Capture traffic:

```bash
tcpdump -i lo -nn 'tcp port 9090'
```

---

# Experiments performed

### Normal connection

```text
client connects
→ server accepts
→ data exchanged
```

### Client sends nothing

```text
client connects
→ server waits in read()
→ timeout
```

### Client disconnects

```text
client closes
→ server observes EOF / connection close
```

### Multiple clients

```text
multiple connections
→ separate worker threads
```

### Port collision

```text
Server A → :9090
Server B → :9090
          ↓
Address already in use
```

### Packet inspection

Used `tcpdump` to observe the TCP handshake, data transfer, and connection closing.

---

# Core mental model

```text
Client
   ↓
IP + Port
   ↓
TCP connection
   ↓
Socket
   ↓
Java application
```

Underneath:

```text
Java Socket API
      ↓
Operating System
      ↓
TCP/IP
      ↓
Network
```

The important lesson is that a backend server isn't just application code. There is an OS, networking stack, connections, resources, and concurrency underneath it.

---

# Why this matters for later labs

This lab gives the foundation for understanding:

```text
TCP
 ↓
HTTP
 ↓
HTTP Server
 ↓
Tomcat
 ↓
Spring Boot
```

The next lab adds **HTTP rules on top of the TCP byte stream**.

## Next

**Lab 2 — HTTP Client**

Topics:

* HTTP request/response model
* request line and methods
* headers
* body
* status line and status codes
* `Content-Length`
* persistent connections / keep-alive
* URL, path and query
* HTTP on top of TCP
