package Desmultiplexer;

import DataLayer.GestorDeDados;
import Desmultiplexer.Operacoes.OperacaoI;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class GestorDeQueues {
    private Map<Integer,QueueOperacao> queues = new HashMap<>();

    public boolean operationListIsValid(List<OperacaoI> operacoes){
        return operacoes.stream().mapToInt(OperacaoI::getTag).filter(e->e>=0).distinct().count() == operacoes.size();//Verifica se todas as tags são >= 0 e se são todas diferentes
    }

    public boolean loadGestorDeQueues(List<OperacaoI> operacoes,GestorDeDados gestorDeDados){
        if(operationListIsValid(operacoes)) {
            for (OperacaoI operacao : operacoes) {
                queues.put(operacao.getTag(), new QueueOperacao(operacao, gestorDeDados));
            }
            return true;
        } else return false;
    }

    public boolean addPedido(TaggedConnection tg) throws IOException {
        Frame f = tg.receive();
        int tag = f.getTag();
        if(tag!=-1) {  // if tag == -1 then close
            QueueOperacao queue = queues.get(f.getTag());
            if(queue!=null)
                queue.inserePedido(new ConnectionPlusByteArray(f.getData(), tg));
            return true;
        }
        else {
            queues.values().forEach(QueueOperacao::stop);  //Parar todas a queues
            return false;
        }
    }
}
