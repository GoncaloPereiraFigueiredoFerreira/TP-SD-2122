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

    @Override
    public int getTag() {
        return tag;
    }

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
                adicionado = gestorDeDados.removeReservasEViagem(utilizador,idReserva); //todo

            if (adicionado)
                sendConfirmacao(tc,f.getNumber(),0,tag); //Removido
            else if(logado==1||logado==0)
                sendConfirmacao(tc,f.getNumber(),1,tag); //ID nao existe
            else sendConfirmacao(tc,f.getNumber(),2,tag); //Falha de segurança

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
