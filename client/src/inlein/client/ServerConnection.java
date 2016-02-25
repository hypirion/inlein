package inlein.client;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import com.hypirion.bencode.*;

public final class ServerConnection implements AutoCloseable {
    private Socket sock;
    private BencodeWriter out;
    private BencodeReader in;
    private LogPrinter lp;
    private boolean connected;

    public static ServerConnection ensureConnected(ServerConnection sc) throws Exception {
        if (sc == null) {
            sc = new ServerConnection();
        }
        if (! sc.tryConnect()) {
            // TODO: Try to start inlein server.
            System.out.println("Inlein server not running!");
            System.exit(1);
        }
        return sc;
    }

    public boolean tryConnect() throws Exception {
        if (connected) {
            return true;
        }
        Integer port = inleinPort();
        if (port == null) {
            return false;
        }

        String hostName = "localhost";
        int portNumber = (int)port;

        sock = new Socket(hostName, portNumber);
        out = new BencodeWriter(sock.getOutputStream());
        in = new BencodeReader(new BufferedInputStream(sock.getInputStream()));
        lp = new LogPrinter(LogPrinter.Level.WARN);
        connected = true;
        return true;
    }

    public ServerConnection() {
        connected = false;
    }

    public Map<String, Object> sendRequest(Map<String, Object> request) throws Exception {
        out.write(request);
        Map<String, Object> ack = readNonlog();
        // verify ack
        if (! (ack.get("type").equals("ack") &&
               request.get("op").equals(ack.get("op")))) {
            throw new ProtocolException();
        }
        // verify return value
        Map<String, Object> ret = readNonlog();
        if (! ret.get("type").equals("response")) {
            throw new ProtocolException();
        }
        if (ret.get("error") instanceof String) {
            throw new Exception((String)ret.get("error"));
        }
        return ret;
    }

    private Map<String, Object> readNonlog() throws Exception {
        while (true) {
            Map<String, Object> m = in.readDict();
            if (m == null) {
                return null;
            }
            if ("log".equals(m.get("type"))) {
                LogPrinter.Level level = LogPrinter.Level.toLevel((String)m.get("level"));
                lp.printLog(level, (String)m.get("msg"));
            } else {
                return m;
            }
        }
    }

    public void close() throws IOException {
        sock.close();
        out.close();
        in.close();
        connected = false;
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
}
