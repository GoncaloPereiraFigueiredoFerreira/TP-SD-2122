package Demultiplexer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private ReentrantLock rLock = new ReentrantLock();
    private ReentrantLock wLock = new ReentrantLock();

    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.dos    = new DataOutputStream(socket.getOutputStream());
        this.dis    = new DataInputStream(socket.getInputStream());
    }

    public void send(Frame frame) throws IOException {
        send(frame.getNumber(), frame.getTag(),frame.getData());
    }

    public void send(int number, int tag, byte[] data) throws IOException {
        wLock.lock();
        try {
            dos.writeInt(number);
            dos.writeInt(tag);
            dos.writeInt(data.length);
            dos.write(data);
            dos.flush();
        } finally { wLock.unlock(); }
    }

    public Frame receive() throws IOException {
        int number, tag, dataSize;
        byte[] data;

        try{
            rLock.lock();
            number   = dis.readInt();
            tag      = dis.readInt();
            dataSize = dis.readInt();
            data     = new byte[dataSize];
            dis.readFully(data);
        } catch (SocketTimeoutException ste){ return null;
        } finally { rLock.unlock(); }

        return new Frame(number, tag, data);
    }

    public void close() throws IOException {
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }
}

