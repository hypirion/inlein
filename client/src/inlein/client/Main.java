package inlein.client;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.net.*;
import com.hypirion.bencode.*;

import inlein.client.tasks.*;

public class Main {
    public static void main(String[] args) throws IOException, BencodeReadException {
        String task;
        String[] taskArgs;
        if (args.length == 0) {
            task = "--help";
            taskArgs = new String[0];
        }
        else if (! args[0].startsWith("--")) {
            task = "--run";
            taskArgs = args;
        } else {
            task = args[0];
            taskArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        Task t = tasks.get(task);
        if (t == null) {
            System.out.println("Inlein has no task with name " + task);
            System.exit(1);
        }

        t.run(null, taskArgs);
        System.exit(0);

        Version.instance.run(null, args);
        System.exit(1);
        Integer port = inleinPort();
        if (port == null) {
            System.out.println("Inlein server not running!");
            System.exit(1);
        }

        String hostName = "localhost";
        int portNumber = (int)port;

        try (
            Socket sock = new Socket(hostName, portNumber);
            BencodeWriter out = new BencodeWriter(sock.getOutputStream());
            BencodeReader in = new BencodeReader(new BufferedInputStream(sock.getInputStream()));
        ) {
            Map<String, Object> hm = new HashMap();
            hm.put("op", "ping");
            System.out.println(hm);
            out.write(hm);

            Object fromServer;
            while ((fromServer = in.read()) != null) {
                System.out.println("Server: " + fromServer);
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }
    }


    /**
     * Returns the home directory to inlein.
     */
    public static String inleinHome() {
        String res = System.getenv("INLEIN_HOME");
        if (res == null) {
            res = new File(System.getProperty("user.home"), ".inlein").getAbsolutePath();
        }
        return res;
    }

    /**
     * Returns the inlein server's port. If the value returned is
     * <code>null</code>, then the server is not running.
     */
    public static Integer inleinPort() throws IOException {
        Path p = Paths.get(inleinHome(), "port");
        if (!p.toFile().exists()) {
            return null;
        }
        byte[] stringPort = Files.readAllBytes(p);
        return Integer.parseInt(new String(stringPort));
    }

    public static final TreeMap<String, Task> tasks = new TreeMap();

    static {
        Help h = new Help(tasks);
        tasks.put(h.taskname, h);
        tasks.put(Version.instance.taskname, Version.instance);
    }
}
