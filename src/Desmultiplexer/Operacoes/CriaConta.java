package Desmultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Desmultiplexer.ConnectionPlusByteArray;
import Desmultiplexer.Frame;
import Desmultiplexer.TaggedConnection;

import java.io.*;

public class CriaConta implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag = 0;

    public CriaConta(){}

    public CriaConta(ConnectionPlusByteArray cpba,GestorDeDados gestorDeDados){
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
        Thread t = new Thread(new CriaConta(cpba,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
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
