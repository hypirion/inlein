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
              "Runs a clojure repl, optionally running a script and/or loading extra dependencies");
    }

    private void die() {
        System.out.println("repl expects [--deps dep1[;dep2...]] [file [args...]]\n" +
                           "where deps are semicolon-separated maven coordinates (with group optional if type+classifier also omitted)\n" +
                           "i.e. [group:]artifact[:type[:classifier]]:version (e.g. clj-time:0.14.0 / org.jclouds:jclouds:1.0:jar:jdk15)");
        System.exit(1);
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        String coords = "reply:0.3.7";
        int script = 0;
        if (args.length > 0 && "--deps".equals(args[0])) {
            if (args.length < 2) {
                die();
            }
            coords += ";" + args[1];
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
