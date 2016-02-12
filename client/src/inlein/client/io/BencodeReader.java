package inlein.client.io;

import java.io.*;

public final class BencodeReader implements Closeable {
    private InputStream input;

    public BencodeReader(InputStream input) {
        this.input = input;
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
}
