package inlein.client;

import java.util.*;
import java.io.*;
import java.net.*;
import com.hypirion.bencode.*;

public class Main {
    public static void main(String[] args) throws IOException, BencodeReadException {
        if (args.length != 2) {
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

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
}
