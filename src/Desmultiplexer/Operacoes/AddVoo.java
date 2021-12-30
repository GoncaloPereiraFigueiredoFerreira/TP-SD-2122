package Desmultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Desmultiplexer.ConnectionPlusByteArray;
import Desmultiplexer.Frame;
import Desmultiplexer.TaggedConnection;
import java.io.IOException;

public class AddVoo implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;

    public AddVoo(){}

    public AddVoo(ConnectionPlusByteArray cpba,GestorDeDados gestorDeDados){
        this.bytes= cpba.getBytes();
        this.tc= cpba.getTg();
        this.gestorDeDados=gestorDeDados;
    }

    @Override
    public int getTag() {
        return 2;
    }

    @Override
    public void newRun(ConnectionPlusByteArray cpba, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new AddVoo(cpba,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            String origem = new String(bytes);

            Frame f = tc.receive();
            String destino = new String(f.getData());
            int capacidade = f.getTag();

            boolean adicionado = gestorDeDados.addVoo(origem,destino,capacidade);

            if (adicionado)
                tc.send(0,new byte[0]); //Conta criada com sucesso
            else tc.send(1,new byte[0]); //Erro ao criar conta

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
