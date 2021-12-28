package Desmultiplexer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    private GestorDeQueues gestorDeQueues;

    @Override
    public void run() {
        while (true){
            try {
                ServerSocket ss = new ServerSocket(8888);

                while (true){
                    Socket s = ss.accept();
                    gestorDeQueues.addPedido(new TaggedConnection(s));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
