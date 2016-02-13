package inlein.client.io;

import java.io.*;
import java.util.*;

public final class BencodeReader implements Closeable {
    private PushbackInputStream input;

    public BencodeReader(InputStream input) {
        this.input = new PushbackInputStream(input, 1);
    }

    public void close() throws IOException {
        input.close();
    }

    private int forceRead() throws IOException {
        int val = input.read();
        if (val == -1) {
            throw new EOFException();
        }
        return val;
    }

    private int peek() throws IOException {
        int val = input.read();
        if (val == -1) {
            throw new EOFException();
        }
        input.unread(val);
        return val;
    }

    public long readLong() throws IOException, BencodeReadException {
        int initial = forceRead();
        if (initial != 'i') {
            throw new BencodeReadException("Bencoded integer must start with 'i', not '%c'",
                                           initial);
        }
        long val = 0;
        boolean negative = false, readDigit = false;
        while (true) {
            int cur = forceRead();
            if (cur == '-' && !negative && !readDigit) {
                negative = true;
            }
            else if ('0' <= cur && cur <= '9') {
                readDigit = true;
                val *= 10;
                val += cur - '0';
            }
            else if (cur == 'e') {
                if (readDigit) {
                    return negative ? -val : val;
                } else {
                    throw new BencodeReadException("Bencoded integer must contain at least one digit");
                }
            }
            else {
                throw new BencodeReadException("Unexpected character '%c' when reading bencoded long",
                                               cur);
            }
        }
    }

    // len is a positive ascii base-10 encoded integer, immediately followed by
    // a colon
    private int readLen() throws IOException, BencodeReadException {
        boolean readDigit = false;
        int val = 0;
        while (true) {
            int cur = forceRead();
            if ('0' <= cur && cur <= '9') {
                readDigit = true;
                val *= 10;
                val += cur - '0';
            }
            else if (cur == ':') {
                if (readDigit) {
                    return val;
                } else {
                    throw new BencodeReadException("Bencode-length must contain at least one digit");
                }
            }
            else {
                throw new BencodeReadException("Unexpected character '%c' when reading bencode-length of string",
                                               cur);
            }
        }
    }

    public String readString() throws IOException, BencodeReadException {
        int len = readLen();
        // now read until we have the entire thing
        byte[] bs = new byte[len];
        if (len == 0) { // edge case where last value is an empty string
            return "";
        }
        int off = input.read(bs);
        if (off == -1) {
            throw new EOFException();
        }
        while (off != len) {
            int more = input.read(bs, off, len - off);
            if (more == -1) {
                throw new EOFException();
            }
            off += more;
        }
        return new String(bs, "UTF-8");
    }

    public List<Object> readList() throws IOException, BencodeReadException {
        int initial = forceRead();
        if (initial != 'l') {
            throw new BencodeReadException("Bencoded list must start with 'l', not '%c'",
                                           initial);
        }
        ArrayList<Object> al = new ArrayList<Object>();
        while (peek() != 'e') {
            al.add(read());
        }
        input.read(); // remove 'e' that we peeked
        return al;
    }

    public Object read() throws IOException, BencodeReadException {
        int t = peek();
        switch (t) {
        case 'i':
            return readLong();
        case 'l':
            return readList();
        default:
            return readString();
        }
    }
}
