package inlein.client.io;

public class BencodeReadException extends Exception {
    public BencodeReadException(String message, Object... args) {
        super(String.format(message, args));
    }
}
