package Desmultiplexer;

import Desmultiplexer.Operacoes.OperacaoHI;
import Desmultiplexer.Operacoes.OperacaoHello;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private GestorDeQueues gestorDeQueues;

    public Server(){
        List operacoes = new ArrayList();
        operacoes.add(new OperacaoHello());
        operacoes.add(new OperacaoHI());
        gestorDeQueues = new GestorDeQueues(operacoes);
    }
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(8888);

            while (true){
                Socket s = ss.accept();
                if(!gestorDeQueues.addPedido(new TaggedConnection(s)))
                    break; //Se receber uma tag == -1 então vai deixar de receber pedidos
            }

        } catch (IOException e) {
                e.printStackTrace();
        }
    }
}
