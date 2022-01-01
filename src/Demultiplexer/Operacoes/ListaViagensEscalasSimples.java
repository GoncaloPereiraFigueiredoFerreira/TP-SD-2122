package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.Frame;
import Demultiplexer.TaggedConnection;
import Demultiplexer.Viagens;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ListaViagensEscalasSimples implements OperacaoI{
    Frame f;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag=8;

    public ListaViagensEscalasSimples(){}

    public ListaViagensEscalasSimples(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados){
        this.f= f;
        this.tc= tc;
        this.gestorDeDados=gestorDeDados;
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public void newRun(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new ListaViagensEscalasSimples(tc,f,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            List<List<String>> viagens = gestorDeDados.listaViagensExistentes();
            tc.send(f.getNumber(),tag,(Viagens.serialize(viagens)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}