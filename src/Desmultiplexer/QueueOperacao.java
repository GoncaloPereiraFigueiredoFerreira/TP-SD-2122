package Desmultiplexer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class QueueOperacao {
    private Deque<ConnectionPlusByteArray> pedidos = new ArrayDeque<>();
    private final Operacao operacao;
    private ReentrantLock rlock = new ReentrantLock();
    private Condition isEmpty = rlock.newCondition();

    public class Worker implements Runnable{
        @Override
        public void run() {
            while (true){
                while (executaProxPedido());  //Executar todos os pedidos da queue

                try {  //Quando a queue estiver vazia espera
                    isEmpty.await();
                } catch (InterruptedException e) {
                    break;
                }

            }
        }
    }

    public QueueOperacao(Operacao operacao){
        this.operacao=operacao;
    }

    boolean inserePedido(ConnectionPlusByteArray pedido){
        isEmpty.signal();
        return pedidos.offer(pedido);
    }

    boolean executaProxPedido(){
        ConnectionPlusByteArray cpba = pedidos.pop();
        if(cpba!=null){
            operacao.run(cpba.getBytes(),cpba.getTg());
            return true;
        }
        return false;
    }
}
