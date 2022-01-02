package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import DataLayer.Viagem;
import Demultiplexer.ClassesSerializable.InformacaoSobreReserva;
import Demultiplexer.Frame;
import Demultiplexer.TaggedConnection;

import java.io.*;
import java.util.Collection;

public class ReservasUtilizador implements OperacaoI{
    Frame f;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag = 9;

    public ReservasUtilizador(){}

    public ReservasUtilizador(TaggedConnection tc, Frame f, GestorDeDados gestorDeDados){
        this.f=f;
        this.tc= tc;
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
        Thread t = new Thread(new ReservasUtilizador(tc,f,gestorDeDados));
        t.start();
    }

    public void run() {
        try {

            ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);

            String utilizador = ois.readUTF();

            ois.close();
            bais.close();

            Collection<InformacaoSobreReserva> reservas= gestorDeDados.listaViagensUtilizador(utilizador);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeInt(reservas.size());
            for (InformacaoSobreReserva reserva:reservas){
                oos.write(reserva.serialize());
            }
            oos.flush();

            byte[] byteArray = baos.toByteArray();
            tc.send(f.getNumber(),tag, byteArray);

            oos.close();
            baos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
