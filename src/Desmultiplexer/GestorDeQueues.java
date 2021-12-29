package Desmultiplexer;

import Desmultiplexer.Operacoes.OperacaoI;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorDeQueues {
    private Map<Integer,QueueOperacao> queues = new HashMap<>();

    public GestorDeQueues(List<OperacaoI> operacoes){
        for (int i=0;i<operacoes.size();i++){
            queues.put(i,new QueueOperacao(operacoes.get(i)));
        }
    }

    public boolean addPedido(TaggedConnection tg) throws IOException {
        Frame f = tg.receive();
        int tag = f.getTag();
        if(tag!=-1) {  // if tag == -1 then close
            QueueOperacao queue = queues.get(f.getTag());
            queue.inserePedido(new ConnectionPlusByteArray(f.getData(), tg));
            return true;
        }
        else {
            queues.values().forEach(QueueOperacao::stop);  //Parar todas a queues
            return false;
        }
    }
}
