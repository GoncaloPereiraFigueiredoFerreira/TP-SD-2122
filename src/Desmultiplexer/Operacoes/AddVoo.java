package Desmultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Desmultiplexer.ConnectionPlusByteArray;
import Desmultiplexer.Frame;
import Desmultiplexer.TaggedConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class AddVoo implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag=2;

    public AddVoo(){}

    public AddVoo(ConnectionPlusByteArray cpba,GestorDeDados gestorDeDados){
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
        Thread t = new Thread(new AddVoo(cpba,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);

            String origem = ois.readUTF();
            String destino = ois.readUTF();
            int capacidade = ois.readInt();

            ois.close();
            bais.close();

            boolean adicionado = gestorDeDados.addVoo(origem,destino,capacidade);
            if (adicionado)
                sendConfirmacao(tc,0,tag); //Conta criada com sucesso
            else sendConfirmacao(tc,1,tag); //Erro ao criar conta

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
