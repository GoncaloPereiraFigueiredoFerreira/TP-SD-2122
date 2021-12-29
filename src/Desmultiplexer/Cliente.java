package Desmultiplexer;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Cliente {
    private static TaggedConnection tc;
    private Socket s;

    //Deviamos ter operaçao para saber quais as suas reservas
    //Deviamos retornar a data na reserva

    public static class Thread1 extends Thread{
        String user;
        String pass;

        public Thread1 (String username,String password){
            this.user = username;
            this.pass = password;
        }
        @Override
        public void run(){
            // TODO:: THREAD DE CRIAR CONTA
            //Não sei qual o formato da mensagem
            String msg = user+pass;
            try {
                // Envia uma password e um username
                tc.send(1,(msg).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(100);

                // Recebe uma confirmação de criação de conta
                Frame frame = tc.receive();
                System.out.println("(1) Reply: " + new String(frame.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static class Thread2 extends Thread{
        String user;
        String pass;

        public Thread2 (String username,String password){
            this.user = username;
            this.pass = password;
        }
        @Override
        public void run(){
            // TODO:: THREAD DE CRIAR CONTA
            // Não sei qual o formato da mensagem
            String msg = user+pass;
            try {
                // Envia uma password e um username
                tc.send(2,(msg).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(100);

                // Recebe uma confirmação de login
                Frame frame = tc.receive();
                System.out.println("(1) Reply: " + new String(frame.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static class Thread3 extends Thread{
        String origem;
        String destino;
        int capacidade;
        public Thread3 (String origem,String destino,int capacidade){
            this.origem = origem;
            this.destino = destino;
            this.capacidade=capacidade;
        }
        @Override
        public void run(){
            // TODO:: THREAD DE ADD VOO
            // Não sei qual o formato da mensagem
            String msg = "ola123";
            try {
                // Envia uma origem destino e capacidade
                tc.send(3,(msg).getBytes(StandardCharsets.UTF_8));
                Thread.sleep(100);

                // Recebe uma confirmação da adição
                Frame frame = tc.receive();
                System.out.println("(1) Reply: " + new String(frame.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


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

    public static class Thread5 extends Thread{
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

   public static void main(String[] args){
       Scanner sc = new Scanner(System.in);

       while(true){
           int option = sc.nextInt();
           Thread t=null;
           boolean flag=false;
           try {
               Socket s = new Socket("localhost",9000);
               tc = new TaggedConnection(s);

           } catch (IOException e) { e.printStackTrace(); }

           //Need to know the opcode
           switch (option){
               case 1:
                   MenuInput m1 = new MenuInput("Insira o seu username:","Username:");
                   MenuInput m2 = new MenuInput("Insira o seu password:","Password:");
                   m1.executa();
                   m2.executa();
                   t = new Thread1(m1.getOpcao(),m2.getOpcao());
                   break;
               case 2:
                   MenuInput m3 = new MenuInput("Insira o seu username:","Username:");
                   MenuInput m4= new MenuInput("Insira o seu password:","Password:");
                   m3.executa();
                   m4.executa();
                   t = new Thread2(m3.getOpcao(),m4.getOpcao());
                   break;
               case 3:
                   MenuInput m5 = new MenuInput("Insira a origem:","Origem:");
                   MenuInput m6= new MenuInput("Insira o destino:","Destino:");
                   MenuInput m7= new MenuInput("Insira a capacidade:","Capacidade:");
                   m5.executa();
                   m6.executa();
                   boolean dummyflag;
                   int n = 0;
                   do{
                       dummyflag = false;
                       m7.executa();
                       try{
                           n = Integer.parseInt(m7.getOpcao());

                       }catch (NumberFormatException e){
                           dummyflag = true;
                       }
                   }while(dummyflag);

                   t= new Thread3(m5.getOpcao(),m6.getOpcao(),n);
                   break;
               case 4:
                   //Menu de datas
                   LocalDate l = LocalDate.now();
                   t = new Thread4(l);
                   break;
               case 5:
                   //Menus de origem, destino, e data inicial e final
                    String origem = "";
                    List<String> dest= new ArrayList<>();
                    LocalDate inDate= LocalDate.now(), finDate = LocalDate.now();

                   t= new Thread5(origem,dest,inDate,finDate);
                   break;
               case 6:
                   t = new Thread6();
                   break;
               case 7:
                   t= new Thread7();
                   break;
               case 8:
                   MenuInput m8 = new MenuInput("Insira o codigo de reserva:","Cod Reserva:");
                   t= new Thread8(m8.getOpcao());
                   break;
               case 9:
                   t = new Thread9();
                   break;
                default:
                    flag=true;
                    break;
            }
            if (!flag) t.start();
        }
    }
}

