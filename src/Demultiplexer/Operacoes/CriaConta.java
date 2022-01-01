package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.Frame;
import Demultiplexer.TaggedConnection;

import java.io.*;

public class CriaConta implements OperacaoI{
    Frame f;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag = 0;

    public CriaConta(){}

    public CriaConta(TaggedConnection tc,Frame f,GestorDeDados gestorDeDados){
        this.f=f;
        this.tc= tc;
        this.gestorDeDados=gestorDeDados;
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public void newRun(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new CriaConta(tc,f,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);

            String username = ois.readUTF();
            String password = ois.readUTF();
            boolean admin = ois.readBoolean();

            ois.close();
            bais.close();

            boolean adicionado = gestorDeDados.addUtilizador(username,password,admin);
            if (adicionado)
                sendConfirmacao(tc,0,tag); //Conta criada com sucesso
            else sendConfirmacao(tc,1,tag); //Conta criada com sucesso

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
