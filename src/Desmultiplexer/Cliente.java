package Desmultiplexer;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Cliente {
    private TaggedConnection connect() throws IOException {
        Socket s = new Socket("localhost", 8888);
        return new TaggedConnection(s);
    }
    //Deviamos ter operaçao para saber quais as suas reservas
    //Deviamos retornar a data na reserva

    public boolean criaConta(String username,String password) { //TODO:: THREAD DE CRIAR CONTA | tag 0
        try {
            TaggedConnection tc= connect();
            // Envia uma password e um username
            tc.send(0, (username).getBytes(StandardCharsets.UTF_8));
            tc.send(0, (password).getBytes(StandardCharsets.UTF_8));

            // Recebe uma confirmação de criação de conta
            Frame frame = tc.receive();
            return frame.getData()[0] == (byte) 0;  //0 significa que a conta foi criada, -1 caso contrário
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addVoo(String origem,String destino,int capacidade) { //TODO:: THREAD DE Adicionar Voo | tag 1
        try {
            TaggedConnection tc= connect();
            // Envia uma origem destino e capacidade
            tc.send(1, (origem).getBytes(StandardCharsets.UTF_8));  //Enviar Origem
            tc.send(1, (destino).getBytes(StandardCharsets.UTF_8)); //Enviar destino
            tc.send(1, ByteBuffer.allocate(4).putInt(capacidade).array()); //Enviar capacidade

            // Recebe uma confirmação da adição
            Frame frame = tc.receive();
            return frame.getData()[0] == (byte) 0;  //0 significa que o voo foi criado, -1 caso contrário
        } catch (Exception e) {
            return false;
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

