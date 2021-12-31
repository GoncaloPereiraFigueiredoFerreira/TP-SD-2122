package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.ConnectionPlusByteArray;
import Demultiplexer.TaggedConnection;
import Demultiplexer.Viagens;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ListaVoosPossiveis implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag=4;

    public ListaVoosPossiveis(){}

    public ListaVoosPossiveis(ConnectionPlusByteArray cpba, GestorDeDados gestorDeDados){
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
        Thread t = new Thread(new ListaVoosPossiveis(cpba,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            List<List<String>> viagens = gestorDeDados.listaVoosExistentes();

            tc.send(tag,(Viagens.serialize(viagens)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
