package Desmultiplexer;

import DataLayer.GestorDeDados;
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
    private Worker thread = new Worker();
    private boolean stopCall;
    private GestorDeDados gestorDeDados; //DATA

    public class Worker extends Thread {
        @Override
        public void run() {
            while (!stopCall) {
                try {
                    rlock.lock();
                    while (pedidos.isEmpty())
                        isEmpty.await();
                } catch (InterruptedException e) {
                    stopCall=true;
                } finally {
                    rlock.unlock();
                }

                executaProxPedido();
            }

            while (!pedidos.isEmpty()) //Quando a stop call for true já não precisamos de nos preocupar com locks pois não vão ser recebidos mais pedidos
                executaProxPedido();
        }
    }

    public QueueOperacao(OperacaoI operacao,GestorDeDados gestorDeDados){
        this.gestorDeDados=gestorDeDados;
        this.operacao=operacao;
        this.stopCall=false;
        thread.start();
    }

    public void stop(){
        try {
            rlock.lock();
            if(pedidos.isEmpty())
                thread.interrupt();
            else stopCall=true;
        } finally {
            rlock.unlock();
        }
    }

    public boolean inserePedido(ConnectionPlusByteArray pedido){
        try {
            rlock.lock();
            boolean inserido = pedidos.offer(pedido);
            isEmpty.signal();
            return inserido;
        } finally {
            rlock.unlock();
        }
    }

    private boolean executaProxPedido(){
        ConnectionPlusByteArray cpba;
        try {
            rlock.lock();
            cpba = pedidos.poll();
        } finally {
            rlock.unlock();
        }

        if(cpba!=null){
            operacao.newRun(cpba,gestorDeDados);
            return true;
        }
        return false;
    }
}
