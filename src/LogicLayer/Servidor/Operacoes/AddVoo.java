package LogicLayer.Servidor.Operacoes;

import DataLayer.GestorDeDados;
import LogicLayer.Frame;
import LogicLayer.TaggedConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class AddVoo implements OperacaoI{
    Frame f;
    TaggedConnection tc;
    GestorDeDados gestorDeDados;
    int tag=2;

    public AddVoo(){}

    public AddVoo(TaggedConnection tc,Frame f,GestorDeDados gestorDeDados){
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
    public void newRun(TaggedConnection tc, Frame f, GestorDeDados gestorDeDados) {
        Thread t = new Thread(new AddVoo(tc,f,gestorDeDados));
        t.start();
    }

    public void run() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);

            String origem = ois.readUTF();
            String destino = ois.readUTF();
            int capacidade = ois.readInt();

            ois.close();
            bais.close();

            boolean adicionado = gestorDeDados.addVoo(origem,destino,capacidade);
            if (adicionado)
                sendConfirmacao(tc,0,tag,f.getNumber()); //Voo criada com sucesso
            else sendConfirmacao(tc,1,tag,f.getNumber()); //Erro ao criar conta

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
