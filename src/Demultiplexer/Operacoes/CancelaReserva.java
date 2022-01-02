package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.Frame;
import Demultiplexer.TaggedConnection;

import java.io.*;

public class CancelaReserva implements OperacaoI{
    Frame f;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag = 7;

    public CancelaReserva(){}

    public CancelaReserva(TaggedConnection tc,Frame f,GestorDeDados gestorDeDados){
        this.f = f;
        this.tc= tc;
        this.gestorDeDados=gestorDeDados;
    }

    /**
     * @return  TAG correspondente a operacao
     */
    @Override
    public int getTag() {
        return tag;
    }

    /**
     * Inicializa uma nova operacao para responder ao pedido do cliente
     * @param tc Conexão atual entre o servidor e o cliente
     * @param  f Frame recebido que foi enviado pelo cliente
     * @param  gestorDeDados Camada de dados onde vão ser procuradas as informações relativas aos pedidos do cliente
     */
    @Override
    public void newRun(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new CancelaReserva(tc,f,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);

            int idReserva = ois.readInt();
            String utilizador = ois.readUTF();
            String password = ois.readUTF();

            ois.close();
            bais.close();

            int logado = gestorDeDados.verificaCredenciais(utilizador,password);
            boolean adicionado=false;
            if (logado==1||logado==0)
                adicionado = gestorDeDados.removeReservasEViagem(utilizador,idReserva);

            if (adicionado)
                sendConfirmacao(tc,0,tag,f.getNumber()); //Removido
            else if(logado==1||logado==0)
                sendConfirmacao(tc,1,tag,f.getNumber()); //ID nao existe
            else sendConfirmacao(tc,2,tag,f.getNumber()); //Falha de segurança

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
