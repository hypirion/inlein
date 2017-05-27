package inlein.client.tasks;

import inlein.client.*;
import inlein.client.signals.Registerer;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.*;
import java.util.*;

public final class Run extends Task {

    public static final Run instance = new Run();

    private Run() {
        super("--run",
              "Runs a clojure script with dependencies",
              "Runs a clojure script with dependencies");
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
        Path p = Paths.get(args[0]).toAbsolutePath().normalize();
        req.put("file", p.toString());
        Map<String, Object> reply = conn.sendRequest(req);

        String file = null;
        List<String> fileList = (List<String>) reply.get("files");
        if (fileList.size() == 1) {
            file = fileList.get(0); // == args[0]
        } else {
            Path tmp = Files.createTempFile("inlein-file", ".clj");
            try (FileChannel out = FileChannel.open(tmp, StandardOpenOption.WRITE)) {
                for (String srcFile : fileList) {
                    append(out, Paths.get(srcFile));
                }
            }
            file = tmp.toAbsolutePath().normalize().toString();
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

    private static void append(FileChannel dst, Path src) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(lineSep.getBytes());
        dst.write(bb);
        try (FileChannel srcCh = FileChannel.open(src, StandardOpenOption.READ)) {
            long srcSize = srcCh.size();
            long transferred = srcCh.transferTo(0, srcSize, dst);
            while(transferred != srcSize){
                transferred += srcCh.transferTo(transferred, srcSize - transferred, dst);
            }
        }
    }

    final static String lineSep = System.getProperty("line.separator");
}
