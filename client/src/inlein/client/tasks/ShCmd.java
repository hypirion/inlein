package inlein.client.tasks;

import inlein.client.*;
import inlein.client.signals.Registerer;
import java.nio.file.*;
import java.util.*;

public final class ShCmd extends Task {

    public static final ShCmd instance = new ShCmd();

    private ShCmd() {
        super("--sh-cmd",
              "Prints the shell command a clojure script with dependencies will use",
              "Prints the shell command a clojure script with dependencies will use");
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("run expects at least 1 argument -- the file to run");
            System.exit(1);
        }
        conn = ServerConnection.ensureConnected(conn);
        Map<String, Object> req = new HashMap();
        req.put("op", "jvm-opts");
        Path p = Paths.get(args[0]).toAbsolutePath();
        req.put("file", p.toString());
        Map<String, Object> reply = conn.sendRequest(req);

        String javaCmd = System.getenv("JAVA_CMD");
        if (javaCmd == null) {
            javaCmd = "java";
        }
        ArrayList<String> cmdArgs = new ArrayList<String>();
        cmdArgs.add(javaCmd);
        cmdArgs.addAll((List<String>) reply.get("jvm-opts"));
        cmdArgs.add(String.format("-D$0=%s", args[0]));
        cmdArgs.add("clojure.main");
        for (String arg : args) {
            cmdArgs.add(arg);
        }

        boolean first = true;
        for (String cmdArg : cmdArgs) {
            if (!first) {
                System.out.print(" ");
            }
            if (!containsOnly(cmdArg, CHAR_WHITELIST)) {
                System.out.print("'");
                System.out.print(cmdArg.replace("'", "'\"'\"'"));
                System.out.print("'");
            } else {
                // for prettiness
                System.out.print(cmdArg);
            }
            first = false;
        }
        System.out.println();
    }

    public static final String CHAR_WHITELIST = "abcdefghijklmnopqrstuvwxyz" +
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.-+=_/\\:";


    public static boolean containsOnly(String s, String whitelist) {
        for (int i = 0; i < s.length(); i++) {
            if (whitelist.indexOf(s.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }
}
