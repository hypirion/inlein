# Inlein Protocol

(I am not a protocol designer, so if anything is vague or seems poorly designed,
feel free to comment)

The inplein protocol uses Bencode over a TCP stream. The client first sends a
_request_ to the daemon, and a _response_ is returned from the daemon.

Data transferred between client and daemon is always bencode dicts. The daemon
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
connection or closing it. The client can send as many requests as it would like,
before closing the connection.

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

After a request is sent, the daemon will immediately respond with an ack:

```clj
{:type "ack"
 :op "request"}
```

A client can perform multiple requests in the same TCP stream, but they will be
handled in sequence. The client should – for its own convenience – handle them
sequentially.

### JVM-opts

The most common use of the inlein daemon is to request dependency and
jvm option extraction. This can be done by issuing the command

```clj
{:op "jvm-opts"
 :file "absolute filename"}
```

The response – if successful – will be on the form

```clj
{:type "response"
 :returns "jvm-opts"
 :jvm-args ["-Xms512m" "-cp" "a:b:c"]
 :files ["a.clj" "b.clj"]}
```

The classpath will contain dependencies that are fetched, and `:files` are the
absolute file path needed to run the program. If there are multiple files, then
they must be concatenated in the provided order into e.g. a temporary file,
before being ran.

#### Ping

You can perform a ping to verify that the server is alive. This can be done by
performing

```clj
{:op "ping"}
```

which, if successful, will respond with

```clj
{:type "response"
 :returns "ping"
 :msg "PONG"}
```

#### Shutdown

To shutdown the daemon, one should tell it to shut down like so:

```clj
{:op "shutdown"}
```

This should in turn respond with the value

```clj
{:type "response"
 :returns "shutdown"
 :msg "ok"}
```

The server should not respond to further request from this specific connection,
but should allow others to finish their requests.
