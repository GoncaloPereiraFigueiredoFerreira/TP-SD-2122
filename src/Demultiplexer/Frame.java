package Demultiplexer;

public class Frame {
    private final int number;
    private final int tag;
    private final byte[] data;

    public Frame(int number, int tag, byte[] data) {
        this.number = number; this.tag = tag; this.data = data;
    }

    public int getNumber() {
        return number;
    }

    public int getTag() {
        return tag;
    }

    public byte[] getData() {
        return data.clone();
    }
}