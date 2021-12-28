package Desmultiplexer;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GestorDeQueues {
    private HashMap<Integer,QueueOperacao> queues = new HashMap<>();

    public GestorDeQueues(List<Operacao> operacoes){
        for (int i=0;i<operacoes.size();i++){
            queues.put(i,new QueueOperacao(operacoes.get(i)));
        }
    }

    public boolean addPedido(TaggedConnection tg) throws IOException {
        Frame f = tg.receive();

        QueueOperacao queue = queues.get(f.getTag());
        if(queue!=null){
            queue.inserePedido(new ConnectionPlusByteArray(f.getData(),tg));
            return true;
        } else {
            return false;
        }
    }
}
