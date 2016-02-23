package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.net.*;
import java.util.*;

public final class Shutdown extends Task {

    public static final Shutdown instance = new Shutdown();

    private Shutdown() {
        super("--shutdown",
              "Shuts off the Inlein server",
              "Shuts off the Inlein server");
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        if (args.length != 0) {
            System.out.println("--shutdown takes 0 arguments");
            System.exit(1);
        }
        conn = ServerConnection.ensureConnected(conn);
        Map<String, Object> req = new HashMap();
        req.put("op", "shutdown");
        Map<String, Object> reply = conn.sendRequest(req);
        System.out.println(reply);
    }
}
