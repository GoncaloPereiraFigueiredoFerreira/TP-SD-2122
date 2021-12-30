package Desmultiplexer;

import Desmultiplexer.Exceptions.ServerIsClosedException;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class Cliente {
    private TaggedConnection connect() throws IOException {
        try {
            Socket s = new Socket("localhost", 8888);
            return new TaggedConnection(s);
        } catch (ConnectException ce) {
            return null;
        }
    }
    //Deviamos ter operaçao para saber quais as suas reservas
    //Deviamos retornar a data na reserva

    public int confirmacao(Frame f,int opCode) throws IOException {
        int rValue;
        if(f.getTag()!=opCode) rValue=-2;
        else {
            ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            rValue = ois.readInt();
            ois.close();
            bais.close();
        }
        return rValue;
    }

    public void fecharServidor() throws ServerIsClosedException { //tag -1 //todo mudar void para boolean
        try {
            TaggedConnection tc = connect();
            if(tc==null) throw new ServerIsClosedException();
            // Envia tag -1 para sinalizar fecho
            tc.send(-1, new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDadosCriaConta(TaggedConnection tc,String username,String password,Boolean admin, int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeUTF(username);
        oos.writeUTF(password);
        oos.writeBoolean(admin);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        tc.send(tag, byteArray);

        oos.close();
        baos.close();
    }

    public int criaConta(String username,String password,Boolean administrador) throws ServerIsClosedException{ //tag 0
        try {
            TaggedConnection tc = connect();
            if(tc==null) throw new ServerIsClosedException();
            // Recebe uma confirmação de criação de conta
            sendDadosCriaConta(tc,username,password,administrador,0);
            return confirmacao(tc.receive(),0);
        } catch (IOException e) {
            return -2;
        }
    }

    public void sendDadosLogin(TaggedConnection tc,String username,String password, int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeUTF(username);
        oos.writeUTF(password);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        tc.send(tag, byteArray);

        oos.close();
        baos.close();
    }

    public int login(String username,String password) throws ServerIsClosedException{ //tag 1
        try {
            TaggedConnection tc = connect();
            if(tc==null) throw new ServerIsClosedException();
            // Envia uma password e um username
            sendDadosLogin(tc,username,password,1);
            // Recebe uma confirmação de criação de conta
            return confirmacao(tc.receive(), 1);  //0 significa que efetuou login de utilizador, 1 de admin  e -1 em caso de falha
        } catch (IOException e) {
            return -2;
        }
    }

    public void sendDadosAddVoo(TaggedConnection tc,String origem,String destino,int capacidade, int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeUTF(origem);
        oos.writeUTF(destino);
        oos.writeInt(capacidade);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        tc.send(tag, byteArray);

        oos.close();
        baos.close();
    }

    public int addVoo(String origem,String destino,int capacidade) throws ServerIsClosedException{ // tag 2
        try {
            TaggedConnection tc= connect();
            if(tc==null) throw new ServerIsClosedException();
            // Envia uma origem destino e capacidade
            sendDadosAddVoo(tc,origem,destino,capacidade,2);
            return confirmacao(tc.receive(),2);  //0 significa que o voo foi criado, 1 caso contrário
        } catch (IOException e) {
            return 1;
        }
    }

    public void sendDadosEncerraDia(TaggedConnection tc,String dia,int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeUTF(dia);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        tc.send(tag, byteArray);

        oos.close();
        baos.close();
    }

   public int encerraDia(String dia) throws ServerIsClosedException{ // tag 3
       try {
           TaggedConnection tc= connect();
           if(tc==null) throw new ServerIsClosedException();

           sendDadosEncerraDia(tc,dia,3);
           return confirmacao(tc.receive(),3);  //0 significa que o dia foi fechado, 1 caso contrário
       } catch (IOException e) {
           return 1;
       }
   }
/*
    public static class Thread5 extends Thread{ //Unica thread necessária
        String origem;
        List<String> destinos;
        LocalDate dInf;
        LocalDate dSup;
        public Thread5 (String origem, List<String> destinos, LocalDate dInf,LocalDate dSup){
            this.origem = origem;
            this.destinos = destinos;
            this.dInf = dInf;
            this.dSup = dSup;
        }

        @Override
        public void run(){
            // TODO:: THREAD DE RESERVA
            String msg = "ola123";
            try {
            // Envia origem, lista de destinos, data inferior, data superior
                tc.send(5,(msg).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(100);

            // Recebe o código da reserva (pelos vistos)
                Frame frame = tc.receive();
                System.out.println("(1) Reply: " + new String(frame.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Thread6 extends Thread{
        public Thread6 (){
        }
        @Override
        public void run(){
            // TODO:: THREAD DE LISTA VOOS
            String msg = "ola123";
            try {
            // Apenas faz o pedido
            tc.send(6,(msg).getBytes(StandardCharsets.UTF_8));
            Thread.sleep(100);
            // Recebe lista de voos possíveis
            Frame frame = tc.receive();
            System.out.println("(1) Reply: " + new String(frame.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static class Thread7 extends Thread{
        public Thread7 (){
        }
        @Override
        public void run(){
            // TODO:: THREAD DE LISTA Viagens
            String msg = "ola123";
            try {
                // Apenas faz o pedido
                tc.send(7,(msg).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(100);

                // Recebe lista de viagens possíveis
                Frame frame = tc.receive();
                System.out.println("(1) Reply: " + new String(frame.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Thread8 extends Thread{
        String codReserva;
        public Thread8 (String codigo){
            this.codReserva = codigo;
        }
        @Override
        public void run(){
            // TODO:: THREAD CANCELA RESERVAS

            String msg = "ola123";
            try {
                // Envia o código da reserva
                tc.send(8,(msg).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(100);

                // Recebe confirmação do cancelamento da reserva
                Frame frame = tc.receive();
                System.out.println("(1) Reply: " + new String(frame.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Thread9 extends Thread{
        @Override
        public void run(){
            // TODO:: THREAD ADMIN FECH SERVIDOR
            String msg = "ola123";
            try {
                // Envia Pedido
                tc.send(9,(msg).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(100);

                // Recebe confirmação
                Frame frame = tc.receive();
                System.out.println("(1) Reply: " + new String(frame.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    */
}

