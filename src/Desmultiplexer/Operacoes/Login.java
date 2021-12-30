package Desmultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Desmultiplexer.ConnectionPlusByteArray;
import Desmultiplexer.Frame;
import Desmultiplexer.TaggedConnection;
import java.io.IOException;

public class Login implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;

    public Login(){}

    public Login(ConnectionPlusByteArray cpba,GestorDeDados gestorDeDados){
        this.bytes= cpba.getBytes();
        this.tc= cpba.getTg();
        this.gestorDeDados=gestorDeDados;
    }

    @Override
    public int getTag() {
        return 1;
    }

    @Override
    public void newRun(ConnectionPlusByteArray cpba, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new Login(cpba,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            String username = new String(bytes);
            String password = new String(tc.receive().getData());

            int logado = gestorDeDados.verificaCredenciais(username,password);
            System.out.println(logado);
            tc.send(logado,new byte[0]); //Retornar o estado do login

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}