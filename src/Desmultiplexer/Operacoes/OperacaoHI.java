package Desmultiplexer.Operacoes;

import Desmultiplexer.ConnectionPlusByteArray;
import Desmultiplexer.TaggedConnection;

public class OperacaoHI implements OperacaoI{
    byte[] bytes;
    TaggedConnection tc;

    public OperacaoHI(ConnectionPlusByteArray cpba){
        this.bytes= cpba.getBytes();
        this.tc= cpba.getTg();
    }

    @Override
    public void newRun(ConnectionPlusByteArray cpba) {
        Thread t = new Thread(new OperacaoHI(cpba));
        t.start();
    }

    public void run() {
        System.out.println("hi");
    }
}
