package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.net.*;
import java.util.*;

public final class ShutdownDaemon extends Task {

    public static final ShutdownDaemon instance = new ShutdownDaemon();

    private ShutdownDaemon() {
        super("--shutdown-daemon",
              "Shuts off the Inlein daemon",
              "Shuts off the Inlein daemon");
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        assertArgcount(args, 0);
        if (conn == null) {
            conn = new ServerConnection();
        }
        if (! conn.tryConnect()) {
            System.exit(0);
        }
        Map<String, Object> req = new HashMap();
        req.put("op", "shutdown");
        Map<String, Object> reply = conn.sendRequest(req);
    }
}
