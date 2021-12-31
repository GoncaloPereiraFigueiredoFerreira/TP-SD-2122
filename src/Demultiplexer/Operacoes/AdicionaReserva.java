package Demultiplexer.Operacoes;

import DataLayer.Exceptions.localizacoesInvalidasException;
import DataLayer.Exceptions.numeroLocalizacoesInvalidoException;
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

            //TODO - Dá fix a isto
            int nrLocalizacoes = ois.readInt();

            List<String> localizacoes = new ArrayList<>();
            for (int i=0;i< nrLocalizacoes;i++){
                localizacoes.add(ois.readUTF());
            }
            String utilizador = ois.readUTF();
            String password = ois.readUTF();
            LocalDate dInf = (LocalDate) ois.readObject();
            LocalDate dSup = (LocalDate) ois.readObject();

            ois.close();
            bais.close();

            int sucesso = gestorDeDados.verificaCredenciais(utilizador,password);
            int id=0;
            if(sucesso==0||sucesso==1) {
                try {
                    id = gestorDeDados.fazRevervasViagem(utilizador, localizacoes, dInf, dSup);
                }catch (localizacoesInvalidasException e) {
                    sucesso=2;
                }catch (numeroLocalizacoesInvalidoException e) {
                    sucesso=3;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeInt(sucesso); //sucesso -> -1 login errado, 0||1 login correto, 2 localizacoes invalidas, 3 numero de localizacoes invalidas
            oos.writeInt(id);

            byte[] byteArray = baos.toByteArray();
            tc.send(tag, byteArray);

            oos.close();
            baos.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
