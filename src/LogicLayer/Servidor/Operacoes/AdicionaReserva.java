package LogicLayer.Servidor.Operacoes;

import DataLayer.Exceptions.localizacoesInvalidasException;
import DataLayer.Exceptions.numeroLocalizacoesInvalidoException;
import DataLayer.GestorDeDados;
import DataLayer.InformacaoSobreReserva;
import LogicLayer.Frame;
import LogicLayer.TaggedConnection;

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
            InformacaoSobreReserva reserva = null;

            if(sucesso==0||sucesso==1) {
                try {
                    reserva = gestorDeDados.fazRevervasViagem(utilizador, localizacoes, dInf, dSup);
                }catch (localizacoesInvalidasException e) {
                    sucesso=-2;
                }catch (numeroLocalizacoesInvalidoException e) {
                    sucesso=-3;
                }
            } else sucesso=-1;
            if(reserva==null){
                sucesso=-4;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeInt(sucesso);// sucesso -> 0||1,-1 login errado, -2 localizacoes invalidas, -3 numero de localizacoes invalidas, -4 vaga imposivel
            if(sucesso==0||sucesso==1) reserva.serialize(oos);
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
