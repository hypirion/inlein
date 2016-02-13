package inlein.client.io;

import java.io.*;
import java.util.*;

public final class BencodeWriter {
    private OutputStream out;

    public BencodeWriter(OutputStream out) {
        this.out = out;
    }

    public void writeString(String s) throws IOException {
        byte[] bs = s.getBytes("UTF-8");
        byte[] bsLen = new Integer(bs.length).toString().getBytes("UTF-8");
        out.write(bsLen);
        out.write(':');
        out.write(bs);
    }

    public void writeLong(long l) throws IOException {
        byte[] bs = new Long(l).toString().getBytes("UTF-8");
        out.write('i');
        out.write(bs);
        out.write('e');
    }

    public void writeList(List<Object> list) throws IOException {
        out.write('l');
        for (Object elem : list) {
            write(elem);
        }
        out.write('e');
    }

    public void writeDict(Map<String, Object> map) throws IOException {
        out.write('d');
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            writeString(entry.getKey());
            write(entry.getValue());
        }
        out.write('e');
    }

    public void write(Object o) throws IOException {
        if (o instanceof Long) {
            writeLong((Long) o);
        }
        else if (o instanceof Integer) {
            writeLong(((Integer) o).longValue());
        }
        // Do not support smaller types, at least not for now
        else if (o instanceof List) {
            writeList((List<Object>) o);
        }
        else if (o instanceof Map) {
            writeDict((Map<String,Object>) o);
        }
        else {
            String msg = String.format("Value must either be integer, string, list or map, was %s",
                                       o.getClass().getName());
            throw new IllegalArgumentException(msg);
        }
    }
}
