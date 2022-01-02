package Demultiplexer;

import DataLayer.GestorDeDados;
import Demultiplexer.Operacoes.OperacaoI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server extends Thread {
    private static final int WORKERS_PER_CONNECTION = 100;
    private Map<Integer,OperacaoI> gestorDeOperacoes = new HashMap<>();
    private GestorDeDados gestorDeDados= new GestorDeDados();


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

    public boolean addPedido(TaggedConnection tg,Frame f) throws IOException {
        int tag = f.getTag();
        if(tag!=-1) {  // if tag == -1 then close
            OperacaoI operacao = gestorDeOperacoes.get(f.getTag());
            if(operacao!=null)
                operacao.newRun(tg,f,gestorDeDados);
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
            AtomicBoolean running= new AtomicBoolean(true);

            while(running.get()) {
                Socket s = ss.accept();
                s.setSoTimeout(1000);

                Runnable worker = () -> {
                    try {
                        TaggedConnection c = new TaggedConnection(s);
                        while (running.get()) {
                            Frame frame = c.receive();
                            if(frame != null) {
                                if (frame.getTag() == -1) {
                                    running.set(false);
                                    ss.close();
                                } else addPedido(c, frame);
                            }
                        }
                    } catch (IOException ignored) { }
                };
                new Thread(worker).start();
            }

        }catch (SocketException se){
            System.out.println("Server Closed");
        } catch (IOException e) {
            System.out.println("Erro de conexao");
        }
    }
}
