package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.net.*;
import java.util.*;

public final class Run extends Task {

    public static final Run instance = new Run();

    private Run() {
        super("--run",
              "Runs a clojure script with dependencies",
              "Runs a clojure script with dependencies");
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("run expects at least 1 argument -- the file to run");
            System.exit(1);
        }
        conn = ServerConnection.ensureConnected(conn);
        Map<String, Object> req = new HashMap();
        req.put("op", "jvm-args");
        req.put("file", args[0]);
        Map<String, Object> reply = conn.sendRequest(req);
        System.out.println(reply);
    }
}
