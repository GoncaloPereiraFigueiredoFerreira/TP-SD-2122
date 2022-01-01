package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.ConnectionPlusByteArray;
import Demultiplexer.TaggedConnection;

import java.io.*;

public class CancelaReserva implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag = 7;

    public CancelaReserva(){}

    public CancelaReserva(ConnectionPlusByteArray cpba,GestorDeDados gestorDeDados){
        this.bytes= cpba.getBytes();
        this.tc= cpba.getTg();
        this.gestorDeDados=gestorDeDados;
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public void newRun(ConnectionPlusByteArray cpba, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new CancelaReserva(cpba,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);

            int idReserva = ois.readInt();
            String utilizador = ois.readUTF();
            String password = ois.readUTF();

            ois.close();
            bais.close();

            int logado = gestorDeDados.verificaCredenciais(utilizador,password);
            boolean adicionado=false;
            if (logado==1||logado==0)
                adicionado = gestorDeDados.removeReservasEViagem(utilizador,idReserva); //todo

            if (adicionado)
                sendConfirmacao(tc,0,tag); //Removido
            else if(logado==1||logado==0)
                sendConfirmacao(tc,1,tag); //ID nao existe
            else sendConfirmacao(tc,2,tag); //Falha de segurança

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
