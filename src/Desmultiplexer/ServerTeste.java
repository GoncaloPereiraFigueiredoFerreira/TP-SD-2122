package Desmultiplexer;

import Desmultiplexer.Operacoes.AddVoo;
import Desmultiplexer.Operacoes.CriaConta;
import Desmultiplexer.Operacoes.Login;
import Desmultiplexer.Operacoes.OperacaoI;

import java.util.ArrayList;
import java.util.List;

public class ServerTeste {
    public static void main(String[] args) throws InterruptedException {
        Server s = new Server();

        List<OperacaoI> operacoes = new ArrayList<>();
        operacoes.add(new CriaConta());
        operacoes.add(new AddVoo());
        operacoes.add(new Login());

        if(!s.loadServer(operacoes)){
            System.out.println("Erro ao dar load ao servidor");
            return;
        }
        System.out.println("Server is running");
        s.start();
        s.join();
        System.out.println("Server closed");
    }
}
