package Desmultiplexer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    public Socket socket;
    public DataOutputStream dos;
    public DataInputStream dis;
    public ReentrantLock rLock = new ReentrantLock();
    public ReentrantLock wLock = new ReentrantLock();

    public TaggedConnection(Socket socket) throws IOException {
        this.socket=socket;
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());
    }

    public void send(Frame frame) throws IOException {
        send(frame.getTag(),frame.getData());
    }

    public void send(int tag, byte[] data) throws IOException {
        wLock.lock();
        try {
            dos.writeInt(tag);
            dos.writeInt(data.length);
            dos.write(data);
            dos.flush();
        } finally {
            wLock.unlock();
        }
    }

    public Frame receive() throws IOException {
        byte[] data;
        int tag;
        try{
            rLock.lock();
            tag = dis.readInt();
            data = new byte[dis.readInt()];
            dis.readFully(data);
        }finally {
            rLock.unlock();
        }
        return new Frame(tag,data);
    }

    public void close() throws IOException {
        socket.close();
    }
}
