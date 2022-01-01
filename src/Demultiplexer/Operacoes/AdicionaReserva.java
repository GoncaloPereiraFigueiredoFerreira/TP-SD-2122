package Demultiplexer.Operacoes;

import DataLayer.Exceptions.localizacoesInvalidasException;
import DataLayer.Exceptions.numeroLocalizacoesInvalidoException;
import DataLayer.GestorDeDados;
import Demultiplexer.Frame;
import Demultiplexer.TaggedConnection;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdicionaReserva implements OperacaoI{
    Frame f;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag = 6;

    public AdicionaReserva(){}

    public AdicionaReserva(TaggedConnection tc,Frame f,GestorDeDados gestorDeDados){
        this.f=f;
        this.tc= tc;
        this.gestorDeDados=gestorDeDados;
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public void newRun(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new AdicionaReserva(tc,f,gestorDeDados));
        t.start();
    }

    public void run() {
        try {

            ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);

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
            Integer id=0;
            if(sucesso==0||sucesso==1) {
                try {
                    id = gestorDeDados.fazRevervasViagem(utilizador, localizacoes, dInf, dSup);
                }catch (localizacoesInvalidasException e) {
                    id=-2;
                }catch (numeroLocalizacoesInvalidoException e) {
                    id=-3;
                }
            } else id=-1;
            if(id==null){
                id=-4;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeInt(id); //sucesso -> 0||1 login correto,-1 login errado, -2 localizacoes invalidas, -3 numero de localizacoes invalidas, -4 vaga imposivvel
            oos.flush();

            byte[] byteArray = baos.toByteArray();
            tc.send(f.getNumber(),tag, byteArray);

            oos.close();
            baos.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
