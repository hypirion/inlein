package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.net.*;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

public final class Upgrade extends Task {

    public static final Upgrade instance = new Upgrade();

    private static final String latestUrl = "https://github.com/hyPiRion/inlein/releases/latest";
    private static final String urlFormat = "https://github.com/hyPiRion/inlein/releases/download/%s/%s";
    private static final String clientFormatName = "inlein";
    private static final String daemonFormatName = "inlein-daemon-%s-standalone.jar";

    private Upgrade() {
        super("--upgrade",
              "Upgrades to the specified inlein version, or latest",
              "Upgrades to the specified inlein version, or latest if no argument is provided");
    }

    public void run(ServerConnection conn, String[] args) throws Exception {
        if (args.length >= 2) {
            System.out.printf("Upgrade expects 1 or 0 arguments, but got %d\n", args.length);
            System.exit(1);
        }
        if (Version.getVersion().endsWith("SNAPSHOT")) {
            System.out.println("Cannot upgrade snapshot versions!");
            System.exit(1);
        }
        String vsn = args.length == 0 ? latestVersion() : args[0];
        System.err.printf("Downloading the %s version of inlein\n", vsn);
        String clientName = String.format(clientFormatName, vsn);
        String daemonName = String.format(daemonFormatName, vsn);
        Path clientPath = Paths.get(Utils.inleinHome(), "clients", clientName);
        Path daemonPath = Paths.get(Utils.inleinHome(), "daemons", daemonName);
        // TODO: d/l SHA to speed things up? Check d/l is correct?
        // TODO: Run in parallel?
        Utils.downloadFile(String.format(urlFormat, vsn, clientName), clientPath);
        Utils.downloadFile(String.format(urlFormat, vsn, daemonName), daemonPath);
        // then override the jar currently running
        String inleinFile = System.getProperty("inlein.client.file");
        if (inleinFile == null) {
            System.out.println("Could not detect what the client file is named");
            System.exit(1);
        }
        Path inleinPath = Paths.get(inleinFile);
        while (Files.isSymbolicLink(inleinPath)) {
            inleinPath = Files.readSymbolicLink(inleinPath);
        }
        Set<PosixFilePermission> fperms = Files.getPosixFilePermissions(inleinPath);
        Files.copy(clientPath, inleinPath, StandardCopyOption.REPLACE_EXISTING);
        Files.setPosixFilePermissions(inleinPath, fperms);
        ShutdownDaemon.instance.run(conn, new String[0]);
        System.out.printf("Upgraded to %s of inlein.\n", vsn);
    }

    public static void downloadDaemon(String vsn) throws Exception {
        String daemonName = String.format(daemonFormatName, vsn);
        Path daemonPath = Paths.get(Utils.inleinHome(), "daemons", daemonName);
        Utils.downloadFile(String.format(urlFormat, vsn, daemonName), daemonPath);
    }

    public static String latestVersion() throws Exception {
        URL url = new URL(latestUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);

        if (conn.getResponseCode() != 302) {
            throw new Exception("Unable to detect latest inlein version");
        }
        String loc = conn.getHeaderField("location");
        return loc.substring(loc.lastIndexOf('/') + 1);
    }
}
