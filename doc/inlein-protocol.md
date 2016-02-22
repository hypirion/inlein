# Inlein Protocol

(I am not a protocol designer btw, this is all randomly created)

Synchronous Bencode over a TCP stream. The client first sends a _request_ to the
server, and a _response_ is returned from the server.

Data transferred between client and server is always bencode dicts. The server
may send log _messages_ before or after responses are sent.

## Log Messages

A log message is, shown as EDN for readability, a bencode dict with the
following contents:

```clojure
{:type "log"
 :level "info"
 :timestamp "timestamp"
 :msg "message"}
```

These are noncritical, and can be omitted by the program reading them. Level can
be either debug, info, warn or error. It is recommended to pass back warnings
and errors to the user.

## Opening and Closing the connection

The client does not have to send anything over the wire after opening a
connection or closing it.

## Requests

The client can ask for different kinds of request. A request is on the form

```clj
{:op "request"
 :extra "params"}
```

and a response is on the form

```clj
{:type "response"
 :returns "type"
 :extra "fields"}
```

If an error makes the request not possible to satisfy, then the response will be
on the shape

```clj
{:type "response"
 :returns "type"
 :error "error message"}
```

As a result, extra fields cannot be named `error`.

After a request is sent, the server will immediately respond with an ack:

```clj
{:type "ack"
 :op "request"}
```

A client can perform multiple requests in the same TCP stream, but should for
its own sanity do them sequentially.

### JVM-params

The most common use of the inlein server is to request dependency and
jvm argument extraction. This can be done by issuing the command

```clj
{:op "jvm-params"
 :file "absolute filename"}
```

The response – if successful – will be on the shape

```clj
{:type "response"
 :returns "jvm-params"
 :classpath ["a" "b" "c"]
 :jvm-args ["-Xms512m"]}
```

#### Deps

```clj
{:type "response"
 :returns "deps"
 :msg "Done" ;; or "Failed"
}
```

#### Pong

```clj
{:type "response"
 :returns "ping"
 :msg "PONG"}
```

#### Shutdown

```clj
{:type "response"
 :returns "shutdown"
 :msg "ok"}
```
