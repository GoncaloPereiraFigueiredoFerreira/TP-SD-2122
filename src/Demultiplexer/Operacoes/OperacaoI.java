package Demultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Demultiplexer.Frame;
import Demultiplexer.TaggedConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public interface OperacaoI extends Runnable{
    /**
     * @return  TAG correspondente a operacao
     */
    public int getTag();

    /**
     * Inicializa uma nova operacao para responder ao pedido do cliente
     * @param tc Conexão atual entre o servidor e o cliente
     * @param  f Frame recebido que foi enviado pelo cliente
     * @param  gestorDeDados Camada de dados onde vão ser procuradas as informações relativas aos pedidos do cliente
     */
    public void newRun(TaggedConnection tc,Frame f, GestorDeDados gestorDeDados);

    /**
     * Envia uma mensagem com apenas um inteiro no byte[] da tagged connection
     * @param tc Conexão atual entre o servidor e o cliente
     * @param  confirmacao Confirmacao que vai ser enviada para o cliente
     * @param  tag TAG correspondente ao tipo de operacao executada
     * @param numeroOperacao Identificador do numero de operacao que foi executado
     */
    public default void sendConfirmacao(TaggedConnection tc,int confirmacao, int tag,int numeroOperacao) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeInt(confirmacao);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        tc.send(numeroOperacao,tag, byteArray);

        oos.close();
        baos.close();
    }
}
