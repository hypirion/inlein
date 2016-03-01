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
        try (ReadableByteChannel src = Channels.newChannel((new URL(url)).openStream());
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
        Files.move(tmp, p, StandardCopyOption.ATOMIC_MOVE,
                   StandardCopyOption.REPLACE_EXISTING);
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
