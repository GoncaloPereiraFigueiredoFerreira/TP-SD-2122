package Desmultiplexer;

import DataLayer.GestorDeDados;
import Desmultiplexer.Operacoes.OperacaoHI;
import Desmultiplexer.Operacoes.CriaConta;
import Desmultiplexer.Operacoes.OperacaoI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private GestorDeQueues gestorDeQueues;
    private GestorDeDados gestorDeDados= new GestorDeDados(); //todo contrutor de server com opcao de dar load

    public Server(){
        List<OperacaoI> operacoes = new ArrayList<>();
        operacoes.add(new CriaConta());
        operacoes.add(new OperacaoHI());
        gestorDeQueues = new GestorDeQueues(operacoes,gestorDeDados);
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
