package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.Frame;
import Demultiplexer.TaggedConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class EncerraDia implements OperacaoI{
    Frame f;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag=3;

    public EncerraDia(){}

    public EncerraDia(TaggedConnection tc,Frame f,GestorDeDados gestorDeDados){
        this.tc=tc;
        this.f=f;
        this.gestorDeDados=gestorDeDados;
    }

    /**
     * @return  TAG correspondente a operacao
     */
    @Override
    public int getTag() {
        return tag;
    }

    /**
     * Inicializa uma nova operacao para responder ao pedido do cliente
     * @param tc Conexão atual entre o servidor e o cliente
     * @param  f Frame recebido que foi enviado pelo cliente
     * @param  gestorDeDados Camada de dados onde vão ser procuradas as informações relativas aos pedidos do cliente
     */
    @Override
    public void newRun(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new EncerraDia(tc,f,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);

            LocalDate dia = (LocalDate) ois.readObject();

            ois.close();
            bais.close();

            try {
                gestorDeDados.closeDay(dia);
                sendConfirmacao(tc,0,tag,f.getNumber()); //Dia fechado
            } catch (DateTimeParseException dtpe){
                sendConfirmacao(tc,1,tag,f.getNumber()); //Erro ao fechar o dia
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
