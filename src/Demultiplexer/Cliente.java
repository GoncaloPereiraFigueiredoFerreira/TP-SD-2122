package Demultiplexer;

import DataLayer.Viagem;
import Demultiplexer.Exceptions.ServerIsClosedException;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private Demultiplexer m;
    private String utilizador;
    private String password;
    private boolean logado = false;

    public Cliente(Demultiplexer m){ this.m = m; }

    //Deviamos ter operaçao para saber quais as suas reservas
    //Deviamos retornar a data na reserva

    private int confirmacao(Frame f) throws IOException {
        int rValue;

        ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
        ObjectInputStream ois = new ObjectInputStream(bais);
        rValue = ois.readInt();
        ois.close();
        bais.close();

        return rValue;
    }

    //TODO - ver situacao de ServerIsClosedException
    public void fecharServidor(int number) throws ServerIsClosedException { //tag -1 //todo mudar void para boolean
        try {
            // Envia tag -1 para sinalizar fecho
            m.send(number, -1, new byte[0], true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDadosCriaConta(int number, String username, String password, Boolean admin, int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeUTF(username);
        oos.writeUTF(password);
        oos.writeBoolean(admin);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        m.send(number, tag, byteArray, false);

        oos.close();
        baos.close();
    }

    //TODO - ver situacao de ServerIsClosedException
    public int criaConta(int number, String username,String password,Boolean administrador) throws ServerIsClosedException{ //tag 0
        try {
            // Recebe uma confirmação de criação de conta
            sendDadosCriaConta(number,username,password,administrador,0);
            int confirm = confirmacao(m.receive(number));
            m.finishedReceivingMessages(number);
            return confirm;
        } catch (IOException e) {
            return -2;
        }
    }

    private void sendDadosLogin(int number, String username,String password, int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeUTF(username);
        oos.writeUTF(password);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        m.send(number, tag, byteArray, false);

        oos.close();
        baos.close();
    }

    //TODO - ver situacao de ServerIsClosedException
    public int login(int number, String username,String password) throws ServerIsClosedException{ //tag 1
        try {
            // Envia uma password e um username
            sendDadosLogin(number,username,password,1);
            // Recebe uma confirmação de criação de conta
            int confirmacao = confirmacao(m.receive(number));
            m.finishedReceivingMessages(number);
            if(confirmacao==0||confirmacao==1){
                this.utilizador=username;
                this.password=password;
                this.logado=true;
            }
            return confirmacao;  //0 significa que efetuou login de utilizador, 1 de admin  e -1 em caso de falha
        } catch (IOException e) {
            return -2;
        }
    }

    private void sendDadosAddVoo(int number, String origem,String destino,int capacidade, int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeUTF(origem);
        oos.writeUTF(destino);
        oos.writeInt(capacidade);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        m.send(number, tag, byteArray, false);

        oos.close();
        baos.close();
    }

    //TODO - ver situacao de ServerIsClosedException
    public int addVoo(int number, String origem,String destino,int capacidade) throws ServerIsClosedException{ // tag 2
        try {
            // Envia uma origem destino e capacidade
            sendDadosAddVoo(number,origem,destino,capacidade,2);
            int confirmacao = confirmacao(m.receive(number));  //0 significa que o voo foi criado, 1 caso contrário
            m.finishedReceivingMessages(number);
            return confirmacao;
        } catch (IOException e) {
            return 1;
        }
    }

    private void sendDadosEncerraDia(int number, LocalDate dia,int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(dia);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        m.send(number, tag, byteArray, false);

        oos.close();
        baos.close();
    }

    //TODO - ver situacao de ServerIsClosedException
   public int encerraDia(int number, LocalDate dia) throws ServerIsClosedException{ // tag 3
       try {
           sendDadosEncerraDia(number,dia,3);
           int confirmacao = confirmacao(m.receive(number));  //0 significa que o dia foi fechado, 1 caso contrário
           m.finishedReceivingMessages(number);
           return confirmacao;
       } catch (IOException e) {
           return 1;
       }
   }

    //TODO - ver situacao de ServerIsClosedException
    public List<List<String>> listaVoosPossiveis(int number) throws ServerIsClosedException{ //tag 4
        try {
            m.send(number, 4, new byte[0], false);
            Frame f = m.receive(number);
            m.finishedReceivingMessages(number);

            List<List<String>> viagens= null;
            if(f.getTag()==4) {
                viagens = Viagens.deserialize(f.getData());
            }
            return viagens;  //se for null entao houve erro de conexão
        } catch (IOException e) {
            return null;
        }
    }

    private void sendDadosViagensEscalas(int number, String origem, String destino, int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeUTF(origem);
        oos.writeUTF(destino);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        m.send(number, tag, byteArray, false);

        oos.close();
        baos.close();
    }

    //TODO - ver situacao de ServerIsClosedException
    public List<List<String>> listaViagensEscalas(int number, String origem, String destino) throws ServerIsClosedException{ //tag 5
        try {
            sendDadosViagensEscalas(number,origem,destino,5);
            Frame f = m.receive(number);
            m.finishedReceivingMessages(number);

            List<List<String>> viagens= null;
            if(f.getTag()==5) {
                viagens = Viagens.deserialize(f.getData());
            }
            return viagens;  //se for null entao houve erro de conexão
        } catch (IOException e) {
            return null;
        }
    }

    private void sendDadosReserva(int number, List<String> localizacoes, LocalDate dInf,LocalDate dSup,int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeInt(localizacoes.size());
        for (String s:localizacoes)
            oos.writeUTF(s);
        oos.writeUTF(utilizador);
        oos.writeUTF(password);
        oos.writeObject(dInf);
        oos.writeObject(dSup);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        m.send(number, tag, byteArray, false);

        oos.close();
        baos.close();
    }

    //TODO - ver situacao de ServerIsClosedException
    public void fazReserva(int number, List<String> localizacoes, LocalDate dInf,LocalDate dSup) throws ServerIsClosedException{ //tag 6
        try {
            sendDadosReserva(number,localizacoes,dInf,dSup,6);

            try {
                Frame f = m.receive(number);
                m.finishedReceivingMessages(number);

                ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);

                int bemSucedido = ois.readInt();
                int id = ois.readInt();

                ois.close();
                bais.close();

                if(f.getTag()==6) {
                    if (bemSucedido == 0 || bemSucedido == 1) //Todo expandir bem sucedidos
                        System.out.println("ID da reserva: " + id);
                    else if (bemSucedido == 2)
                        System.out.println("Localizacoes inseridas são inválidas");
                    else if (bemSucedido == 3)
                        System.out.println("Numero de localizacoes inserido é invalido");
                    else System.out.println("Error");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {} //todo
    }

    private void sendDadosCancelaReserva(int number, Integer idReserva, int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeInt(idReserva);
        oos.writeUTF(this.utilizador);
        oos.writeUTF(this.password);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        m.send(number, tag, byteArray, false);

        oos.close();
        baos.close();
    }

    //TODO - ver situacao de ServerIsClosedException
    public int cancelaReserva(int number, Integer idReserva) throws ServerIsClosedException{ //tag 7
        try {
            sendDadosCancelaReserva(number, idReserva, 7);
            int confirmacao = confirmacao(m.receive(number));  //0 significa que a reserva foi removida, 1 id nao existe, 2 falha de segurança
            m.finishedReceivingMessages(number);
            return confirmacao;
        } catch (IOException e) {
            return 1;
        }
    }

/*
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

