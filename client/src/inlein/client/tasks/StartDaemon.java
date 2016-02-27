package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.net.*;
import java.util.*;

public final class StartDaemon extends Task {

    public static final StartDaemon instance = new StartDaemon();

    private StartDaemon() {
        super("--start-daemon",
              "Starts the inlein daemon",
              "Starts the inlein daemon");
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        assertArgcount(args, 0);
        conn = ServerConnection.ensureConnected(conn);
        Map<String, Object> req = new HashMap();
        req.put("op", "ping");
        Map<String, Object> reply = conn.sendRequest(req);
        if (reply.get("msg").equals("PONG")) {
            System.out.println("Inlein daemon is now running");
        } else {
            System.out.println("Unable to start inlein daemon");
            System.exit(1);
        }
    }
}
