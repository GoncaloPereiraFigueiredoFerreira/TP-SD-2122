package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.ConnectionPlusByteArray;
import Demultiplexer.TaggedConnection;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdicionaReserva implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag = 6;

    public AdicionaReserva(){}

    public AdicionaReserva(ConnectionPlusByteArray cpba,GestorDeDados gestorDeDados){
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
        Thread t = new Thread(new AdicionaReserva(cpba,gestorDeDados));
        t.start();
    }

    public void run() {
        try {

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);

            List<String> localizacoes = new ArrayList<>();
            for (int i=0;i< ois.readInt();i++){
                localizacoes.add(ois.readUTF());
            }
            String utilizador = ois.readUTF();
            String password = ois.readUTF();
            String dInf = ois.readUTF();
            String dSup = ois.readUTF();

            ois.close();
            bais.close();

            int login = gestorDeDados.verificaCredenciais(utilizador,password);
            int id=0;
            if(login==0||login==1) {
                id = gestorDeDados.fazRevervasViagem(utilizador, localizacoes, LocalDate.parse(dInf), LocalDate.parse(dSup));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeInt(login); //todo verificar as exceptions
            oos.writeInt(id);

            byte[] byteArray = baos.toByteArray();
            tc.send(tag, byteArray);

            oos.close();
            baos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
