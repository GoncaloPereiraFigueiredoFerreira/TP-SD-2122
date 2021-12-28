package Desmultiplexer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private GestorDeQueues gestorDeQueues;

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(8888);

            while (true){
                Socket s = ss.accept();
                if(!gestorDeQueues.addPedido(new TaggedConnection(s)))
                    break; //Se receber uma tag == -1 então vai deixar de receber pedidos
            }

        } catch (IOException e) {
                e.printStackTrace();
        }
    }
}
