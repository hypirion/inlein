package inlein.client.tasks;

import inlein.client.*;
import inlein.client.signals.Registerer;
import java.nio.file.*;
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
