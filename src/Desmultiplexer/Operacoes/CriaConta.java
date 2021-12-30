package Desmultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Desmultiplexer.ConnectionPlusByteArray;
import Desmultiplexer.Frame;
import Desmultiplexer.TaggedConnection;
import java.io.IOException;

public class CriaConta implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;

    public CriaConta(){}

    public CriaConta(ConnectionPlusByteArray cpba,GestorDeDados gestorDeDados){
        this.bytes= cpba.getBytes();
        this.tc= cpba.getTg();
        this.gestorDeDados=gestorDeDados;
    }

    @Override
    public int getTag() {
        return 0;
    }

    @Override
    public void newRun(ConnectionPlusByteArray cpba, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new CriaConta(cpba,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            String username = new String(bytes);
            Frame f = tc.receive();
            String password = new String(f.getData());

            boolean adicionado = gestorDeDados.addUtilizador(username,password,f.getTag()==0);

            if (adicionado)
                tc.send(0,new byte[0]); //Conta criada com sucesso
            else tc.send(1,new byte[0]); //Erro ao criar conta

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
