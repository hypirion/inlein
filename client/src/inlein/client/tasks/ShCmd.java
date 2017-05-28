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

    @SuppressWarnings("unchecked")
    public void run(ServerConnection conn, String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("run expects at least 1 argument -- the file to run");
            System.exit(1);
        }
        conn = ServerConnection.ensureConnected(conn);
        Map<String, Object> req = new HashMap<String, Object>();
        req.put("op", "jvm-opts");
        Path p = Paths.get(args[0]).toAbsolutePath();
        req.put("file", p.toString());
        Map<String, Object> reply = conn.sendRequest(req);

        String file;
        boolean usesTmpFile = false;
        List<String> fileList = (List<String>) reply.get("files");
        if (fileList.size() == 1) {
            file = fileList.get(0); // == args[0]
        } else {
            System.out.print("TMP_FILE=\"$(mktemp /tmp/inlein-XXXXXXXXXXXXX)\"; ");
            file = "\"$TMP_FILE\"";
            usesTmpFile = true;
            System.out.print("cat ");
            for (String fname : fileList) {
                System.out.print(fname + " ");
            }
            System.out.print("> " + file + "; ");
        }

        String javaCmd = System.getenv("JAVA_CMD");
        if (javaCmd == null) {
            javaCmd = "java";
        }
        ArrayList<String> cmdArgs = new ArrayList<String>();
        cmdArgs.add(javaCmd);
        cmdArgs.addAll((List<String>) reply.get("jvm-opts"));
        cmdArgs.add(String.format("-D$0=%s", args[0]));
        cmdArgs.add("clojure.main");
        cmdArgs.add(file);
        for (int i = 1; i < args.length; i++) {
            cmdArgs.add(args[i]);
        }

        boolean first = true;
        for (String cmdArg : cmdArgs) {
            if (!first) {
                System.out.print(" ");
            }
            // Ugggh, this is so hacky :/
            if (containsOnly(cmdArg, CHAR_WHITELIST) || (cmdArg == file && usesTmpFile)) {
                // for prettiness
                System.out.print(cmdArg);
            } else {
                System.out.print("'");
                System.out.print(cmdArg.replace("'", "'\"'\"'"));
                System.out.print("'");
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
