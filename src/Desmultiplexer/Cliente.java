package Desmultiplexer;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Cliente {
    private static TaggedConnection tc;
    private Socket s;

    //Deviamos ter operaçao para saber quais as suas reservas
    //Deviamos retornar a data na reserva

    public static class Thread1 extends Thread{
        @Override
        public void run(){
            // TODO:: THREAD DE CRIAR CONTA
            MenuInput m1 = new MenuInput("Insira o seu username:","Username:");
            MenuInput m2 = new MenuInput("Insira o seu password:","Password:");
            m1.executa();
            m2.executa();


            try {
                // Envia uma password e um username
                tc.send(1,(m1.getOpcao() + m1 .getOpcao()).getBytes(StandardCharsets.UTF_8));
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
        @Override
        public void run(){
            // TODO:: THREAD DE LOGIN
            // Envia uma password e um username
            // Recebe uma confirmação de login conta, e qual a permissão
            System.out.println("OLA 2222222");
        }
    }

    public static class Thread3 extends Thread{
        @Override
        public void run(){
            // TODO:: THREAD DE ADD VOO
            // Envia origem, destino, capacidade
            // Recebe confirmação da adição
            System.out.println("OLA 2222222");
        }
    }

    public static class Thread4 extends Thread{
        @Override
        public void run(){
            // TODO:: ENCERRA DIA
            // Envia data
            // Recebe confirmação da adição
            System.out.println("OLA 2222222");
        }
    }

    public static class Thread5 extends Thread{
        @Override
        public void run(){
            // TODO:: THREAD DE RESERVA
            // Envia origem, lista de destinos, data inferior, data superior
            // Recebe o código da reserva (pelos vistos)
            System.out.println("OLA 2222222");
        }
    }

    public static class Thread6 extends Thread{
        @Override
        public void run(){
            // TODO:: THREAD DE LISTA VOOS
            // Apenas faz o pedido
            // Recebe lista de voos possíveis
            System.out.println("OLA 2222222");
        }
    }


    public static class Thread7 extends Thread{
        @Override
        public void run(){
            // TODO:: THREAD DE LISTA VIAGENS
            // Apenas faz o pedido
            // Recebe lista de viagens possíveis
            System.out.println("OLA 2222222");
        }
    }

    public static class Thread8 extends Thread{
        @Override
        public void run(){
            // TODO:: THREAD CANCELA RESERVAS
            // Envia o código da reserva
            // Recebe confirmação do cancelamento da reserva
            System.out.println("OLA 2222222");
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
                   t = new Thread1();
                   break;
               case 2:
                   t = new Thread2();
                   break;
               case 3:
                   t= new Thread3();
                   break;
               case 4:
                   t = new Thread4();
                   break;
               case 5:
                   t= new Thread5();
                   break;
               case 6:
                   t = new Thread6();
                   break;
               case 7:
                   t= new Thread7();
                   break;
               case 8:
                   t= new Thread8();
                   break;
                default:
                    flag=true;
                    break;
            }
            if (!flag) t.start();
        }
    }
}

