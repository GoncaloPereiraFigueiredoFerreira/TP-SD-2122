package Desmultiplexer;

public interface Operacao {
    public void run(byte[] bytes,TaggedConnection tc);
}
