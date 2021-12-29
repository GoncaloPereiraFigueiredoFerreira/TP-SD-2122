package Desmultiplexer.Operacoes;

import DataLayer.GestorDeDados;
import Desmultiplexer.ConnectionPlusByteArray;
import Desmultiplexer.TaggedConnection;

public interface OperacaoI extends Runnable{
    public void newRun(ConnectionPlusByteArray cpba, GestorDeDados gestorDeDados);
}
