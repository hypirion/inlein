package inlein.client;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.net.*;


/**
 * Static class with a lot of utility functions.
 */
public final class Utils {
    public static void downloadFile(String url, Path p) throws Exception {
        Path tmp = Files.createTempFile(p.toFile().getName(), ".jar");

        System.err.println("Downloading " + url + "...");
        try (ReadableByteChannel src = Channels.newChannel(getURLConnection(url).getInputStream());
             FileChannel dst = new FileOutputStream(tmp.toFile()).getChannel()) {
            final ByteBuffer buf = ByteBuffer.allocateDirect(32 * 1024 * 1024);

            while(src.read(buf) != -1) {
                buf.flip();
                dst.write(buf);
                buf.compact();
            }
            buf.flip();
            while(buf.hasRemaining()) {
                dst.write(buf);
            }
        }
        p.toFile().getCanonicalFile().getParentFile().mkdirs();
        Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
    }

    private static URLConnection getURLConnection(String url) throws IOException {
        Proxy proxy = getProxy();
        URL toConnectTo = new URL(url);
        if (proxy == null) {
            return toConnectTo.openConnection();
        }
        else {
            return toConnectTo.openConnection(proxy);
        }
    }

    private static Proxy getProxy() {
        String proxyHost = System.getProperty("http.proxyHost");
        try
        {
            int proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
            if (proxyHost != null)
            {
                InetSocketAddress sa = new InetSocketAddress(proxyHost, proxyPort);
                return new Proxy(Proxy.Type.HTTP, sa);
            }
        }
        catch (NumberFormatException ignored) {}
        return null;
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
