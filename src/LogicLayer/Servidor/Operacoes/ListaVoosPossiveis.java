package LogicLayer.Servidor.Operacoes;

import DataLayer.GestorDeDados;
import LogicLayer.Frame;
import LogicLayer.TaggedConnection;
import LogicLayer.ClassesSerializable.Viagens;

import java.io.IOException;
import java.util.List;

public class ListaVoosPossiveis implements OperacaoI{
    Frame f;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag=4;

    public ListaVoosPossiveis(){}

    public ListaVoosPossiveis(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados){
        this.f= f;
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
        Thread t = new Thread(new ListaVoosPossiveis(tc,f,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            List<List<String>> viagens = gestorDeDados.listaVoosExistentes();

            tc.send(f.getNumber(),tag,(Viagens.serialize(viagens)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
