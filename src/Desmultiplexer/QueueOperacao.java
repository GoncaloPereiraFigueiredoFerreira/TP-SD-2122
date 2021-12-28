package Desmultiplexer;

import Desmultiplexer.Operacoes.OperacaoI;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class QueueOperacao {
    private Deque<ConnectionPlusByteArray> pedidos = new ArrayDeque<>();
    private final OperacaoI operacao;
    private ReentrantLock rlock = new ReentrantLock();
    private Condition isEmpty = rlock.newCondition();
    private Worker thread;

    public class Worker extends Thread{
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

    public QueueOperacao(OperacaoI operacao){
        this.operacao=operacao;
        thread.start();
    }

    public void stop(){
        thread.interrupt();
    }

    boolean inserePedido(ConnectionPlusByteArray pedido){
        isEmpty.signal();
        return pedidos.offer(pedido);
    }

    boolean executaProxPedido(){
        ConnectionPlusByteArray cpba = pedidos.pop();
        if(cpba!=null){
            operacao.newRun(cpba);
            return true;
        }
        return false;
    }
}
