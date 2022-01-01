package Demultiplexer;

import DataLayer.GestorDeDados;
import Demultiplexer.Operacoes.OperacaoI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server extends Thread {
    private Map<Integer,OperacaoI> gestorDeOperacoes = new HashMap<>();
    private GestorDeDados gestorDeDados= new GestorDeDados(); //todo contrutor de server com opcao de dar load


    public boolean operationListIsValid(List<OperacaoI> operacoes){
        return operacoes.stream().mapToInt(OperacaoI::getTag).filter(e->e>=0).distinct().count() == operacoes.size();//Verifica se todas as tags são >= 0 e se são todas diferentes
    }

    public boolean loadServer(List<OperacaoI> operacoes){
        if(operationListIsValid(operacoes)) {
            for (OperacaoI operacao : operacoes) {
                gestorDeOperacoes.put(operacao.getTag(),operacao);
            }
            return true;
        } else return false;
    }

    public boolean addPedido(TaggedConnection tg) throws IOException {
        Frame f = tg.receive();
        int tag = f.getTag();
        if(tag!=-1) {  // if tag == -1 then close
            OperacaoI operacao = gestorDeOperacoes.get(f.getTag());
            if(operacao!=null)
                operacao.newRun(new ConnectionPlusByteArray(f.getData(),tg),gestorDeDados);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(8888);

            while (true){
                Socket s = ss.accept();
                if(!addPedido(new TaggedConnection(s)))
                    break; //Se receber uma tag == -1 então vai deixar de receber pedidos
            }

        } catch (IOException e) {
                e.printStackTrace();
        }
    }
}
