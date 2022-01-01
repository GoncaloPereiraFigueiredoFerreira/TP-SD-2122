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

public class ListaViagensEscalas implements OperacaoI{
    Frame f;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag=5;

    public ListaViagensEscalas(){}

    public ListaViagensEscalas(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados){
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
        Thread t = new Thread(new ListaViagensEscalas(tc,f,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);

            String origem = ois.readUTF();
            String destino = ois.readUTF();

            ois.close();
            bais.close();

            List<List<String>> viagens = gestorDeDados.listaViagensExistentes(origem,destino);

            tc.send(tag,(Viagens.serialize(viagens)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
