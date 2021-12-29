package Desmultiplexer;

import Desmultiplexer.Exceptions.ServerIsClosedException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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


    public void fecharServidor() throws ServerIsClosedException { //tag -1
        try {
            TaggedConnection tc = connect();
            if(tc==null) throw new ServerIsClosedException();
            // Envia tag -1 para sinalizar fecho
            tc.send(-1, new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int criaConta(String username,String password,Boolean administrador) throws ServerIsClosedException{ //tag 0
        try {
            TaggedConnection tc = connect();
            if(tc==null) throw new ServerIsClosedException();
            // Envia uma password e um username
            tc.send(0, (username).getBytes(StandardCharsets.UTF_8));
            if(administrador)
                tc.send(0, (password).getBytes(StandardCharsets.UTF_8));
            else tc.send(1, (password).getBytes(StandardCharsets.UTF_8));
            // Recebe uma confirmação de criação de conta
            Frame frame = tc.receive();
            return frame.getTag();  //0 significa que a conta foi criada, 1 caso contrário
        } catch (IOException e) {
            return 1;
        }
    }
    public int login(String username,String password) throws ServerIsClosedException{ //tag 1
        try {
            System.out.println("Logging");
            TaggedConnection tc = connect();
            if(tc==null) throw new ServerIsClosedException();
            // Envia uma password e um username
            tc.send(1, (username).getBytes(StandardCharsets.UTF_8));
            tc.send(1, (password).getBytes(StandardCharsets.UTF_8));
            // Recebe uma confirmação de criação de conta
            Frame frame = tc.receive();
            return frame.getTag();  //0 significa que efetuou login de utilizador, 1 de admin  e -1 em caso de falha
        } catch (IOException e) {
            return 1;
        }
    }

    public int addVoo(String origem,String destino,int capacidade) throws ServerIsClosedException{ //TODO:: THREAD DE Adicionar Voo | tag 2
        try {
            TaggedConnection tc= connect();
            if(tc==null) throw new ServerIsClosedException();
            // Envia uma origem destino e capacidade
            tc.send(2, (origem).getBytes(StandardCharsets.UTF_8));  //Enviar Origem
            tc.send(capacidade, (destino).getBytes(StandardCharsets.UTF_8)); //Enviar destino e capacidade

            // Recebe uma confirmação da adição
            Frame frame = tc.receive();
            return frame.getTag();  //0 significa que o voo foi criado, 1 caso contrário
        } catch (IOException e) {
            return 1;
        }
    }

/*
    public static class Thread4 extends Thread{
        LocalDate date;

        public Thread4 (LocalDate date){
            this.date = date;
        }
        @Override
        public void run(){
            // Envia uma password e um username
            // TODO:: ENCERRA DIA
            String msg = "ola123";
            try {
                tc.send(4,(msg).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(100);

                // Recebe uma confirmação de criação de conta
                Frame frame = tc.receive();
                System.out.println("(1) Reply: " + new String(frame.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

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

