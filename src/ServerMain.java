import LogicLayer.Servidor.Operacoes.*;
import LogicLayer.Servidor.Server;

import java.util.ArrayList;
import java.util.List;

public class ServerMain {
    public static void main(String[] args) throws InterruptedException {
        Server s = new Server();

        List<OperacaoI> operacoes = new ArrayList<>();
        operacoes.add(new CriaConta());
        operacoes.add(new AddVoo());
        operacoes.add(new Login());
        operacoes.add(new EncerraDia());
        operacoes.add(new ListaViagensEscalas());
        operacoes.add(new AdicionaReserva());
        operacoes.add(new ListaVoosPossiveis());
        operacoes.add(new CancelaReserva());
        operacoes.add(new ListaViagensEscalasSimples());
        operacoes.add(new ReservasUtilizador());

        if(!s.loadServer(operacoes)){
            System.out.println("Erro ao dar load ao servidor");
            return;
        }
        System.out.println("Server is running");
        s.start();
        s.join();
    }
}
