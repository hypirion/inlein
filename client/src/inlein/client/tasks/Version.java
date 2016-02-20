package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.net.*;
import java.util.*;

public final class Version extends Task {

    public static final Version instance = new Version();

    private Version() {
        super("--version",
              "Prints the currently running Inlein version.",
              "Prints the currently running Inlein version.");
    }

    public void run(ServerConnection conn, String[] args) {
        assertArgcount(args, 0);
        System.out.printf("Inlein %s on Java %s %s\n",
                          getVersion(),
                          System.getProperty("java.version"),
                          System.getProperty("java.vm.name"));
    }

    public synchronized static String getVersion() {
            String version = null;
            try {
                Properties p = new Properties();
                InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("META-INF/maven/inlein/client/pom.properties");
                if (is != null) {
                    p.load(is);
                    version = p.getProperty("version", "");
                }
            } catch (Exception e) {}
            return version == null ? "" : version;
    }
}
