package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.net.*;
import java.util.*;

public final class RestartDaemon extends Task {

    public static final RestartDaemon instance = new RestartDaemon();

    private RestartDaemon() {
        super("--restart-daemon",
              "Restarts the inlein daemon",
              "Restarts the inlein daemon. Equivalent to `inlein --shutdown-daemon && inlein start-daemon`.");
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        ShutdownDaemon.instance.run(conn, args);
        // Sleep slightly, to increase probability that the daemon has actually
        // shut down. TODO: Lock here to ensure consistency?
        Thread.sleep(100);
        StartDaemon.instance.run(null, args);
    }
}
