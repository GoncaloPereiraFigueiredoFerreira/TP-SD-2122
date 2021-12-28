package Desmultiplexer;

public class ConnectionPlusByteArray {
    private final byte[] bytes;
    private final TaggedConnection tg;

    public ConnectionPlusByteArray(byte[] bytes,TaggedConnection tg){
        this.bytes=bytes.clone();
        this.tg=tg;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public TaggedConnection getTg() {
        return tg;
    }
}
