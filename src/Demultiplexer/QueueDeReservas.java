package Demultiplexer;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class QueueDeReservas {
    private ReentrantLock rl = new ReentrantLock();
    private Deque<Integer> queue = new LinkedList<>(); //TODO acrescentar queues de failed adicions ou fazer um tuplo

    public void addReserva(Integer id){
        try {
            rl.lock();
            queue.add(id);
        }finally {
            rl.unlock();
        }
    }
    public Deque<Integer> getReservas(){
        try {
            rl.lock();
            return queue;
        }finally {
            this.queue= new LinkedList<>();
            rl.unlock();
        }
    }
}
