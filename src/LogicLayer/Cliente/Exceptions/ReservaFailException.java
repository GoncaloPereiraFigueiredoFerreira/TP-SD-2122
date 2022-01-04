package LogicLayer.Cliente.Exceptions;

public class ReservaFailException extends Exception{
    public ReservaFailException(){
        super();
    }
    public ReservaFailException(String msg){
        super(msg);
    }
}
