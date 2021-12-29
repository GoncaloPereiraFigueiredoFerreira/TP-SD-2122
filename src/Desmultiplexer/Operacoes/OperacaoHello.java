package Desmultiplexer.Operacoes;

import Desmultiplexer.ConnectionPlusByteArray;
import Desmultiplexer.TaggedConnection;

public class OperacaoHello implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;

    public OperacaoHello(){
    }

    public OperacaoHello(ConnectionPlusByteArray cpba){
        this.bytes= cpba.getBytes();
        this.tc= cpba.getTg(); //Falta clone
    }

    @Override
    public void newRun(ConnectionPlusByteArray cpba) {
        Thread t = new Thread(new OperacaoHello(cpba));
        t.start();
    }

    public void run() {
        System.out.println("Hello");
    }
}
