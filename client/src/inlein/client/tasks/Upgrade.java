package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;

public final class Upgrade extends Task {

    public static final Upgrade instance = new Upgrade();

    private static final String latestUrl = "https://github.com/hyPiRion/inlein/releases/latest";
    private static final String urlFormat = "https://github.com/hyPiRion/inlein/releases/download/%s/%s";
    private static final String clientFormatName = "inlein";
    private static final String localClientFormatName = "inlein-%s";
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
        System.err.printf("Downloading version %s of inlein\n", vsn);
        String clientName = String.format(clientFormatName, vsn);
        String daemonName = String.format(daemonFormatName, vsn);
        String localName = String.format(localClientFormatName, vsn);
        Path clientPath = Paths.get(Utils.inleinHome(), "clients", localName);
        Path daemonPath = Paths.get(Utils.inleinHome(), "daemons", daemonName);
        Future<Void> clientDl = downloadSha256(String.format(urlFormat, vsn, clientName), clientPath);
        Future<Void> daemonDl = downloadSha256(String.format(urlFormat, vsn, daemonName), daemonPath);
        clientDl.get();
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
        daemonDl.get();
        System.out.printf("Upgraded to %s of inlein.\n", vsn);
    }


    public static void downloadDaemon(String vsn) throws Exception {
        String daemonName = String.format(daemonFormatName, vsn);
        Path daemonPath = Paths.get(Utils.inleinHome(), "daemons", daemonName);
        downloadSha256(String.format(urlFormat, vsn, daemonName), daemonPath).get();
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

    private static Future<Void> downloadSha256(String url, Path path) {
        FutureTask<Void> ft = new FutureTask<Void>(new ShaDownloader(url, path));
        new Thread(ft).start();
        return ft;
    }

    private static class ShaDownloader implements Callable<Void> {
        private final String url;
        private final Path path;

        ShaDownloader(String url, Path path) {
            this.url = url;
            this.path = path;
        }

        public Void call() throws Exception {
            boolean outdated = true;
            File f = path.toFile();
            if (f.exists()) {
                String actualSha = fileSha(f);
                String expectedSha = internetSha(url);
                outdated = ! expectedSha.equals(actualSha);
                // TODO: Emit warning message if actual != expected?
            }
            if (outdated) {
                Utils.downloadFile(url, path);
            }
            return null;
        }

        private static String fileSha(File f) throws Exception {
            try (InputStream is = new BufferedInputStream(new FileInputStream(f))) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] buf = new byte[16384];
                int len;
                while ((len = is.read(buf)) > 0) {
                    digest.update(buf, 0, len);
                }
                return DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
            }
        }

        private static String internetSha(String url) throws Exception {
            // I decided to make the .sha256-files compatible with sha256sum,
            // which is why the split is here.
            String upstreamFile = Utils.downloadFile(url + ".sha256");
            return upstreamFile.split(" ")[0];
        }
    }
}
