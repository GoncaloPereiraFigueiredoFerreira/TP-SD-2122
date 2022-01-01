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
    private QueueDeReservas reservas = new QueueDeReservas();
    private String utilizador;
    private String password;
    private boolean logado=false;


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

    private int confirmacao(Frame f,int opCode) throws IOException {
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

    private void sendDadosCriaConta(TaggedConnection tc,String username,String password,Boolean admin, int tag) throws IOException {
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

    private void sendDadosLogin(TaggedConnection tc,String username,String password, int tag) throws IOException {
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
            int confirmacao = confirmacao(tc.receive(), 1);
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

    private void sendDadosAddVoo(TaggedConnection tc,String origem,String destino,int capacidade, int tag) throws IOException {
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

    private void sendDadosEncerraDia(TaggedConnection tc,LocalDate dia,int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(dia);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        tc.send(tag, byteArray);

        oos.close();
        baos.close();
    }

   public int encerraDia(LocalDate dia) throws ServerIsClosedException{ // tag 3
       try {
           TaggedConnection tc= connect();
           if(tc==null) throw new ServerIsClosedException();

           sendDadosEncerraDia(tc,dia,3);
           return confirmacao(tc.receive(),3);  //0 significa que o dia foi fechado, 1 caso contrário
       } catch (IOException e) {
           return 1;
       }
   }

    public List<List<String>> listaVoosPossiveis() throws ServerIsClosedException{ //tag 4
        try {
            TaggedConnection tc= connect();
            if(tc==null) throw new ServerIsClosedException();

            tc.send(4,new byte[0]);
            Frame f = tc.receive();

            List<List<String>> viagens= null;
            if(f.getTag()==4) {
                viagens = Viagens.deserialize(f.getData());
            }
            return viagens;  //se for null entao houve erro de conexão
        } catch (IOException e) {
            return null;
        }
    }

    private void sendDadosViagensEscalas(TaggedConnection tc,String origem,String destino,int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeUTF(origem);
        oos.writeUTF(destino);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        tc.send(tag, byteArray);

        oos.close();
        baos.close();
    }

    public List<List<String>> listaViagensEscalas(String origem, String destino) throws ServerIsClosedException{ //tag 5
        try {
            TaggedConnection tc= connect();
            if(tc==null) throw new ServerIsClosedException();

            sendDadosViagensEscalas(tc,origem,destino,5);
            Frame f = tc.receive();

            List<List<String>> viagens= null;
            if(f.getTag()==5) {
                viagens = Viagens.deserialize(f.getData());
            }
            return viagens;  //se for null entao houve erro de conexão
        } catch (IOException e) {
            return null;
        }
    }

    private void sendDadosReserva(TaggedConnection tc,List<String> localizacoes, LocalDate dInf,LocalDate dSup,int tag) throws IOException {
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
        tc.send(tag, byteArray);

        oos.close();
        baos.close();
    }

    public void fazReserva(List<String> localizacoes, LocalDate dInf,LocalDate dSup) throws ServerIsClosedException{ //tag 6
        try {
            TaggedConnection tc= connect();
            if(tc==null) throw new ServerIsClosedException();

            sendDadosReserva(tc,localizacoes,dInf,dSup,6);

            try {
                Frame f = tc.receive();

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

    private void sendDadosCancelaReserva(TaggedConnection tc,Integer idReserva,int tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeInt(idReserva);
        oos.writeUTF(this.utilizador);
        oos.writeUTF(this.password);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        tc.send(tag, byteArray);

        oos.close();
        baos.close();
    }

    public int cancelaReserva(Integer idReserva) throws ServerIsClosedException{ //tag 7
        try {
            TaggedConnection tc= connect();
            if(tc==null) throw new ServerIsClosedException();

            sendDadosCancelaReserva(tc,idReserva,7);

            return confirmacao(tc.receive(),7);  //0 significa que a reserva foi removida, 1 id nao existe, 2 falha de segurança
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

