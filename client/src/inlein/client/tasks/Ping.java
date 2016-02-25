package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.net.*;
import java.util.*;

public final class Ping extends Task {

    public static final Ping instance = new Ping();

    private Ping() {
        super("--ping",
              "Pings the inlein server, if it runs.",
              "Pings the inlein server, if it runs. Will not start the server if it is\n"
              + "not running.");
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        if (conn == null) {
            conn = new ServerConnection();
        }
        if (! conn.tryConnect()) {
            System.out.println("Inlein server not running!");
            System.exit(1);
        }
        assertArgcount(args, 0);
        conn = ServerConnection.ensureConnected(conn);
        Map<String, Object> req = new HashMap();
        req.put("op", "ping");
        Map<String, Object> reply = conn.sendRequest(req);
        System.out.println(reply.get("msg"));
    }
}
