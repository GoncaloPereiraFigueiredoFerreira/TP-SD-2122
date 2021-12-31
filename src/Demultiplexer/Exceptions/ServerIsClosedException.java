package Demultiplexer.Exceptions;

public class ServerIsClosedException extends Exception{
    public ServerIsClosedException() {}
    public ServerIsClosedException(String errorMessage) {
        super(errorMessage);
    }
}
