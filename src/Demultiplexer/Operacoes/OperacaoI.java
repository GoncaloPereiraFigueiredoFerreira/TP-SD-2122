package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.Frame;
import Demultiplexer.TaggedConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public interface OperacaoI extends Runnable{
    public int getTag();
    public void newRun(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados);

    public default void sendConfirmacao(TaggedConnection tc,int confirmacao, int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeInt(confirmacao);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        tc.send(tag, byteArray);

        oos.close();
        baos.close();
    }
}
