package inlein.client.tasks;

import inlein.client.*;
import java.nio.file.*;
import java.util.*;

public final class Deps extends Task {

    public static final Deps instance = new Deps();

    private Deps() {
        super("--deps",
              "Retrieves the dependencies for a script",
              "Retrieves the dependencies for a script");
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("deps expects exactly 1 argument -- the file to run");
            System.exit(1);
        }
        conn = ServerConnection.ensureConnected(conn);
        Map<String, Object> req = new HashMap();
        req.put("op", "jvm-opts");
        Path p = Paths.get(args[0]).toAbsolutePath();
        req.put("file", p.toString());
        Map<String, Object> reply = conn.sendRequest(req);
        System.out.println("Dependencies fetched");
    }
}
