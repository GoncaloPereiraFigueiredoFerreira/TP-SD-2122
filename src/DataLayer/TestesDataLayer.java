package DataLayer;

import DataLayer.Exceptions.*;

import java.time.LocalDate;
import java.util.*;

public class TestesDataLayer {
    /*
    // ********************* TESTES RESERVAS *********************

    public static class ThreadAdd extends Thread{
        Reservas reservas;
        List<String> listaDeViajantesAadicionar;
        int capacidade;

        public ThreadAdd(Reservas reservas, List<String> listaDeViajantesAadicionar, int capacidade){
            this.reservas = reservas;
            this.listaDeViajantesAadicionar = listaDeViajantesAadicionar;
            this.capacidade = capacidade;
        }

        @Override
        public void run(){
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(String s : listaDeViajantesAadicionar)
                reservas.addViajante(s, capacidade);
        }
    }

    public static class ThreadRm extends Thread{
        Reservas reservas;
        List<String> listaDeViajantesAremover;
        int capacidade;

        public ThreadRm(Reservas reservas, List<String> listaDeViajantesAremover, int capacidade){
            this.reservas = reservas;
            this.listaDeViajantesAremover = listaDeViajantesAremover;
            this.capacidade = capacidade;
        }

        @Override
        public void run(){
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(String s : listaDeViajantesAremover)
                reservas.removeViajante(s);
        }
    }*/



    // ********************* TESTES VOOS *********************

    /*
    public static class ThreadAdd extends Thread{
        Voo voo;
        String nome;
        LocalDate date;

        public ThreadAdd(Voo voo, String nome, LocalDate date){
            this.voo  = voo;
            this.nome = nome;
            this.date = date;
        }

        @Override
        public void run(){
            if(nome.equals("Alex")) {
                try {
                    System.out.println("Lock: " + voo.lock(date));
                    System.out.println("Reservado(" + nome + "): " + voo.addViajante(nome, date));
                    System.out.println("Eliminada reserva(" + nome + ") "); voo.removeViajante(nome, date);
                } finally {
                    System.out.println("Unlock: " + voo.unlock(date));
                }
            }else {
                System.out.println("Reservado(" + nome + "): " + voo.addViajante(nome, date));
                System.out.println("Eliminada reserva(" + nome + ") "); voo.removeViajante(nome, date);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> viajantes = listaViajantes();

        Voo voo = new Voo("Braga","Nova Iorque", 5);
        LocalDate date = LocalDate.now();

        //voo.registarVooNovoDia(date);
        try {
            //System.out.println("Pode Reservar: " + voo.podeReservar(date));

            Thread[] threads = new Thread[6];

            threads[0] = new ThreadAdd(voo, "Alex", date);
            threads[1] = new ThreadAdd(voo, "Luis", date);
            threads[2] = new ThreadAdd(voo, "Bronze", date);
            threads[3] = new ThreadAdd(voo, "Ganso", date);
            threads[4] = new ThreadAdd(voo, "Xiko", date);
            //voo.removeViajante("Luis", date);
            // System.out.println("Reserva eliminada: Luis");
            threads[5] = new ThreadAdd(voo, "Diogo", date);

            for(Thread t : threads)
                t.start();

            for(Thread t : threads)
                t.join();

            System.out.println(voo);
        }
        finally {
            //System.out.println("Unlock: " + voo.unlock(date));
        }

    }
     */



    // ********************* Gestor de Dados *********************

    public static void main(String[] args) {
        GestorDeDados gd = new GestorDeDados();

        //Utilizadores
        gd.addUtilizador("Alex","1234",true);
        gd.addUtilizador("Luis","12345",false);
        gd.addUtilizador("Xiko","123456",false);
        gd.printUtilizadores();

        //Voos
        gd.addVoo("Lisboa","Braga",20);
        gd.addVoo("Lisboa","Porto",25);
        gd.addVoo("Lisboa","Guimaraes",30);
        gd.addVoo("Lisboa","Guarda",35);
        gd.addVoo("Braga","Porto",20);
        gd.addVoo("Guimaraes","Braga",20);
        gd.addVoo("Guimaraes","Guarda",20);
        gd.addVoo("Guarda","Guimaraes",20);
        gd.printVoos();

        //Listagem de viagens
        List<List<String>> list = gd.listaViagensExistentes();
        System.out.println("Tamanho: " + list.size() + "  |  " + list);
        System.out.println("Tamanho(origem: lisboa - destino: porto): "+ gd.listaViagensExistentes("Lisboa","Porto").size()  + "  |  " + gd.listaViagensExistentes("Lisboa","Porto"));

        //Fecha dia
        gd.closeDay(LocalDate.now());

        //Reserva de viagem
        List<String> listaDeLocaisAlex = new ArrayList<>();
        listaDeLocaisAlex.add("Lisboa");
        listaDeLocaisAlex.add("Guimaraes");
        listaDeLocaisAlex.add("Braga");
        listaDeLocaisAlex.add("Porto");

        List<String> listaDeLocaisLuis = new ArrayList<>(listaDeLocaisAlex.subList(0,3));
        listaDeLocaisLuis.add("Tokyo");

        try {
            System.out.println("Id Reserva Viagem: " + gd.fazRevervasViagem("Alex", listaDeLocaisAlex, LocalDate.now(), LocalDate.now().plusDays(1)));
        }
        catch (numeroLocalizacoesInvalidoException nlie){
            System.out.println("Alex inseriu nr invalido de localizacoes");
        }
        catch (localizacoesInvalidasException lie){
            System.out.println("Alex localizacoes invalidas");
        }

        try {
            System.out.println("Id Reserva Viagem: " + gd.fazRevervasViagem("Luis", listaDeLocaisLuis, LocalDate.now(), LocalDate.now().plusDays(1)));
        }
        catch (numeroLocalizacoesInvalidoException nlie){
            System.out.println("Luis inseriu nr invalido de localizacoes");
        }
        catch (localizacoesInvalidasException lie){
            System.out.println("Luis localizacoes invalidas");
        }


        System.out.println("\n\nAlex e Luis fizeram reserva");
        gd.printviagens();
        gd.printVoos();

        //Remover reserva
        System.out.println("\n\nAlex removeu reserva");
        gd.removeReservasEViagem("Luis", 1);
        gd.printVoos();


    }



    public static List<String> listaViajantes(){
        List<String> viajantes = new ArrayList<>();

        viajantes.add("Alex1");
        viajantes.add("Alex2");
        viajantes.add("Alex3");
        viajantes.add("Alex4");
        viajantes.add("Alex5");
        viajantes.add("Alex6");
        viajantes.add("Alex7");
        viajantes.add("Alex8");
        viajantes.add("Alex9");
        viajantes.add("Alex0");
        viajantes.add("Banana1");
        viajantes.add("Banana2");
        viajantes.add("Banana3");
        viajantes.add("Banana4");
        viajantes.add("Banana5");
        viajantes.add("Banana6");
        viajantes.add("Banana7");
        viajantes.add("Banana8");
        viajantes.add("Banana9");
        viajantes.add("Banana0");
        viajantes.add("Cao1");
        viajantes.add("Cao2");
        viajantes.add("Cao3");
        viajantes.add("Cao4");
        viajantes.add("Cao5");
        viajantes.add("Cao6");
        viajantes.add("Cao7");
        viajantes.add("Cao8");
        viajantes.add("Cao9");
        viajantes.add("Cao0");
        viajantes.add("Dado1");
        viajantes.add("Dado2");
        viajantes.add("Dado3");
        viajantes.add("Dado4");
        viajantes.add("Dado5");
        viajantes.add("Dado6");
        viajantes.add("Dado7");
        viajantes.add("Dado8");
        viajantes.add("Dado9");
        viajantes.add("Dado0");
        return viajantes;
    }
}
