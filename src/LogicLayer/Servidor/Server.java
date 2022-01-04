package LogicLayer.Servidor;

import DataLayer.GestorDeDados;
import LogicLayer.Frame;
import LogicLayer.Servidor.Operacoes.OperacaoI;
import LogicLayer.TaggedConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Server extends Thread {
    private final Map<Integer,OperacaoI> gestorDeOperacoes = new HashMap<>();
    private final GestorDeDados gestorDeDados= new GestorDeDados();

    /**
     * Verifica se a lista de operacoes é valida
     * @param operacoes Lista de operacoes que obedecem a interface OperacaoI
     * @return true caso as operacoes não tenham TAG's em comum e a tag seja positiva
     */
    private boolean operationListIsValid(List<OperacaoI> operacoes){
        return operacoes.stream().mapToInt(OperacaoI::getTag).filter(e->e>=0).distinct().count() == operacoes.size();//Verifica se todas as tags são >= 0 e se são todas diferentes
    }

    /**
     * Carrega um mapa de operacoes com as operacoes disponibilizadas
     * @param operacoes Lista de operacoes que obedecem a interface OperacaoI
     * @return true caso as operacoes não tenham TAG's em comum e a tag seja positiva e o mapa tenha dado load
     */
    public boolean loadServer(List<OperacaoI> operacoes){
        if(operationListIsValid(operacoes)) {
            for (OperacaoI operacao : operacoes) {
                gestorDeOperacoes.put(operacao.getTag(),operacao);
            }
            return true;
        } else return false;
    }

    /**
     * Cria uma thread com o mapa correspondente a tag fornecida no frame
     * @param tg Conexão atual entre o servidor e o cliente
     * @param  f Frame enviado pelo cliente
     * @return true caso a tag corresponda a uma operacao do servidor e tenha sido criada uma thread;
     *         false caso contrario
     */
    public boolean addPedido(TaggedConnection tg, Frame f) throws IOException {
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

    /**
     * Liga o servidor pronto para receber pedidos na porta 8888
     */
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(8888);
            AtomicBoolean running= new AtomicBoolean(true);
            ReentrantLock rl = new ReentrantLock();

            rl.lock();
            while(running.get()) {
                rl.unlock();

                Socket s = ss.accept();
                s.setSoTimeout(1000);

                Runnable worker = () -> {
                    try {
                        TaggedConnection c = new TaggedConnection(s);
                        rl.lock();
                        while (running.get()) {
                            rl.unlock();
                            Frame frame = c.receive();
                            if(frame != null) {
                                if (frame.getTag() == -1) {
                                    running.set(false);
                                    ss.close();
                                } else addPedido(c, frame);
                            }
                            rl.lock();
                        }
                        rl.unlock();
                    } catch (IOException ignored) { }
                };
                new Thread(worker).start();

                rl.lock();
            }
            rl.unlock();

        }catch (SocketException se){
            System.out.println("Server Closed");
        } catch (IOException e) {
            System.out.println("Erro de conexao");
        }
    }
}
