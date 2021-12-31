package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.ConnectionPlusByteArray;
import Demultiplexer.TaggedConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class EncerraDia implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag=3;

    public EncerraDia(){}

    public EncerraDia(ConnectionPlusByteArray cpba,GestorDeDados gestorDeDados){
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
        Thread t = new Thread(new EncerraDia(cpba,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);

            LocalDate dia = (LocalDate) ois.readObject();

            ois.close();
            bais.close();

            try {
                gestorDeDados.closeDay(dia);
                sendConfirmacao(tc,0,tag); //Dia fechado
            } catch (DateTimeParseException dtpe){
                sendConfirmacao(tc,1,tag); //Erro ao fechar o dia
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
