package inlein.client.tasks;

import inlein.client.*;
import inlein.client.signals.Registerer;
import java.nio.file.*;
import java.util.*;

public final class Repl extends Task {

    public static final Repl instance = new Repl();

    private Repl() {
        super("--repl",
              "Runs a clojure repl, optionally running a script and/or loading extra dependencies",
              "Runs a clojure repl, optionally running a script and/or loading extra dependencies.\n" +
              "Takes arguments: [--deps dep1[,dep2...]] [file [args...]]\n" +
              "where deps is a comma-separated list of an extended version of maven coordinates:\n" +
              "group:artifact[:type[:classifier]]:version, where if type and classifier are omitted\n" +
              "then group and version are also optional, defaulting to the same as artifact and LATEST respectively\n" +
              "and version ranges use semicolons instead of commas (to avoid conflicting with the separator)\n" +
              "e.g. org.clojure:clojure, clj-time:0.14.0, org.jclouds:jclouds:jar:jdk17:[2.0.0;2.1)");
    }

    private void die() {
        System.out.println("repl expects [--deps dep1[,dep2...]] [file [args...]]\n");
        System.exit(1);
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        String coords = "com.hypirion:inlein-repl:0.1.0";
        int script = 0;
        if (args.length > 0 && "--deps".equals(args[0])) {
            if (args.length < 2) {
                die();
            }
            coords = args[1] + "," + coords;
            script = 2;
        }
        conn = ServerConnection.ensureConnected(conn);
        Map<String, Object> req = new HashMap<>();
        req.put("op", "jvm-opts");
        req.put("deps", coords);
        if (script < args.length) req.put("file", Paths.get(args[script]).toAbsolutePath().toString());
        Map<String, Object> reply = conn.sendRequest(req);

        String javaCmd = System.getenv("JAVA_CMD");
        if (javaCmd == null) {
            javaCmd = "java";
        }
        ArrayList<String> cmdArgs = new ArrayList<String>();
        cmdArgs.add(javaCmd);
        cmdArgs.addAll((List<String>) reply.get("jvm-opts"));
        if (script < args.length) {
            cmdArgs.add(String.format("-D$0=%s", args[script]));
        }
        cmdArgs.add("reply.ReplyMain");
        cmdArgs.add("--standalone");
        if (script < args.length) {
            cmdArgs.add("--init");
            for (int i = script; i < args.length; ++i) {
                cmdArgs.add(args[i]);
            }
        }

        ProcessBuilder pb = new ProcessBuilder(cmdArgs);
        pb.inheritIO();
        Process proc = pb.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new ProcessKiller(proc)));
        Registerer.dropSignals();
        proc.waitFor();
        System.exit(proc.exitValue());
    }

    private static class ProcessKiller implements Runnable {
        final Process proc;
        ProcessKiller(Process proc) {
            this.proc = proc;
        }

        public void run() {
            proc.destroy();
        }
    }
}
