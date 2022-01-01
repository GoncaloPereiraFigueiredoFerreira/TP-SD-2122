package Demultiplexer;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer {
    private static class Entry{
        private final Deque<Frame> queue = new ArrayDeque<>();
        private final Condition cond;

        public Entry(ReentrantLock lock){ cond = lock.newCondition(); }

        public void addMsg(Frame frame){
            queue.add(frame);
        }

        public Frame getMsg(){
            return queue.poll();
        }

        public Condition getCond(){
            return cond;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "queue=" + queue +
                    '}';
        }
    }

    private TaggedConnection tc;
    private IOException ioe            = null;
    private boolean keepDemultiplexing = true;
    private Map<Integer, Entry> queues = new HashMap<>();
    private ReentrantLock rtlock       = new ReentrantLock();

    public Demultiplexer(TaggedConnection tc) throws IOException {
        this.tc = tc;
    }

    public void start() {

        Thread demultiplexingThread = new Thread(() -> {
            while (keepDemultiplexing) {
                try {
                    rtlock.lock();

                    //Receive frame
                    Frame frame = tc.receive();

                    if(frame != null) {
                        //Get entry
                        int number = frame.getNumber();
                        Entry entry = queues.get(number);

                        //Ignores messages without a corresponding entry
                        if (entry != null) {
                            entry.addMsg(frame);
                            entry.getCond().signal();
                        }
                    }
                } catch (IOException ioe) {
                    this.ioe = ioe;
                    keepDemultiplexing = false;
                    queues.values().forEach(entry -> entry.getCond().signal());
                } finally { rtlock.unlock(); }

                Thread.yield();
            }

            try { tc.close(); }
            catch (IOException ioe) { this.ioe = ioe; }
        });

        demultiplexingThread.start();
    }

    public Frame receive(int number) throws IOException {
        Frame frame = null;

        try {
            rtlock.lock();

            Entry entry = queues.get(number);

            //Entry has to be created before expecting a receive
            if (entry != null) {
                while ((frame = entry.getMsg()) == null) {
                    entry.getCond().await();
                    if (ioe != null) { throw ioe; }
                }
            }
        } catch (InterruptedException ignored) {
        } finally { rtlock.unlock(); }

        return frame;
    }

    public void send(int number, int tag, byte[] arr, boolean oneWay) throws IOException {
        send(new Frame(number, tag, arr), oneWay);
    }

    public void send(Frame frame, boolean oneWay) throws IOException {

        if (frame == null) throw new IllegalArgumentException();

        int number = frame.getNumber();

        try {
            rtlock.lock();

            //Verifies the existence of the entry needed to receive the answers
            if (!oneWay) {
                Entry entry = queues.get(number);
                if (entry == null) {
                    entry = new Entry(rtlock);
                    queues.put(number, entry);
                }
            }

            tc.send(frame);

        } finally { rtlock.unlock(); }
    }

    public void finishedReceivingMessages(int number){
        try{
            rtlock.lock();
            queues.remove(number);
        }finally { rtlock.unlock(); }
    }

    public void close() {
        keepDemultiplexing = false;
    }
}
