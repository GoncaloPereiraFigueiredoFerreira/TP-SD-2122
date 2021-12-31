package Demultiplexer;

import Demultiplexer.Exceptions.ServerIsClosedException;
import UI.MenuInput;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ClienteTester {
    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        Scanner sc = new Scanner(System.in);

        boolean running = true;
        while (running) {

            System.out.println("Insira a proxima operacao");
            int option = sc.nextInt();
            Thread t = null;

            //Need to know the opcode
            try {
                int flag=0;
                switch (option) {
                    case -1 -> running=false; //Fechar cliente
                    case 0 -> cliente.fecharServidor(); //Fechar servidor
                    case 1 -> { //Criar conta cliente
                        MenuInput m1 = new MenuInput("Insira um username:", "Username:");
                        MenuInput m2 = new MenuInput("Insira uma password:", "Password:");
                        m1.executa();
                        m2.executa();
                        flag = cliente.criaConta(m1.getOpcao(), m2.getOpcao(),false);
                        if (flag == 0) System.out.println("Cliente criado com sucesso");
                        else if (flag == 1) System.out.println("Falha ao criar cliente");
                    }
                    case 2 -> { //Criar conta admin
                        MenuInput m1 = new MenuInput("Insira um username:", "Username:");
                        MenuInput m2 = new MenuInput("Insira uma password:", "Password:");
                        m1.executa();
                        m2.executa();
                        flag = cliente.criaConta(m1.getOpcao(), m2.getOpcao(),true);
                        if (flag == 0) System.out.println("Aministrador criado com sucesso");
                        else if (flag == 1) System.out.println("Falha ao criar administrador");
                    }
                    case 3 -> { //Login
                        MenuInput m1 = new MenuInput("Insira o seu username:", "Username:");
                        MenuInput m2 = new MenuInput("Insira o seu password:", "Password:");
                        m1.executa();
                        m2.executa();
                        flag = cliente.login(m1.getOpcao(), m2.getOpcao());
                        if (flag == -1) System.out.println("Falha no login");
                        else if (flag == 0) System.out.println("Cliente logado com sucesso");
                        else if (flag == 1) System.out.println("Administrador logado com sucesso");
                    }
                    case 4 -> { //Adicionar voo
                        MenuInput m5 = new MenuInput("Insira a origem:", "Origem:");
                        MenuInput m6 = new MenuInput("Insira o destino:", "Destino:");
                        MenuInput m7 = new MenuInput("Insira a capacidade:", "Capacidade:");
                        m5.executa();
                        m6.executa();
                        boolean dummyflag;
                        int n = 0;
                        do {
                            dummyflag = false;
                            m7.executa();
                            try {
                                n = Integer.parseInt(m7.getOpcao());

                            } catch (NumberFormatException e) {
                                dummyflag = true;
                            }
                        } while (dummyflag);
                        flag = cliente.addVoo(m5.getOpcao(), m6.getOpcao(), n);
                        if (flag == 0) System.out.println("Voo adicionado com sucesso");
                        else if (flag == 1) System.out.println("Falha ao adicionar voo");
                    }

                    case 5 -> { //Encerrar um dia

                        MenuInput m = new MenuInput("Insira a data com o seguinte formato \"YYYY-MM-DD\":", "Data:");
                        m.executa();

                        try {
                            LocalDate opcao = LocalDate.parse(m.getOpcao());
                            flag = cliente.encerraDia(opcao);
                            if (flag == 0) System.out.println("Dia fechado com sucesso");
                            else if (flag == 1) System.out.println("Falha fechar o dia");
                        } catch (DateTimeParseException dtpe){
                            System.out.println("Inseriu uma data com o formato errado");
                        }
                    }
                    case 6 -> { //Listar viagens existentes

                        MenuInput m1 = new MenuInput("Insira a origem da sua viagem:", "Origem:");
                        MenuInput m2 = new MenuInput("Insira a destino da sua viagem:", "Destino:");
                        m1.executa();
                        m2.executa();
                        //Menu de datas
                        String origem = m1.getOpcao();
                        String destino = m2.getOpcao();
                        List<List<String>> viagens = cliente.listaViagensEscalas(origem,destino);
                        if (viagens == null) System.out.println("Falha de conexao");
                        else if (viagens.size() == 0) System.out.println("Nao existem viagens para estes destinos");
                        else System.out.println(Viagens.toStringOutput(viagens,origem,destino));
                    }
                    case 7 -> { //Listar voos possiveis
                        System.out.println("A carregar voos possiveis");

                        List<List<String>> viagens = cliente.listaVoosPossiveis();

                        if (viagens == null) System.out.println("Falha de conexao");
                        else if (viagens.size() == 0) System.out.println("Nao existem voos possiveis");
                        else System.out.println(Viagens.toStringOutput(viagens));
                    }
                    case 8 -> { //Faz reserva

                        MenuInput m1 = new MenuInput("Insira a data inferior com o seguinte formato \"YYYY-MM-DD\":", "Data:");
                        MenuInput m2 = new MenuInput("Insira a data superior com o seguinte formato \"YYYY-MM-DD\":", "Data:");
                        MenuInput m3 = new MenuInput("Insira a origem da sua viagem:", "Origem:"); //todo alterar localizacoes
                        m1.executa();
                        try {
                            LocalDate.parse(m1.getOpcao());
                            m2.executa();
                            try {
                                LocalDate.parse(m2.getOpcao());

                                m3.executa();
                                String origem = m3.getOpcao();

                            }catch (DateTimeParseException dtpe) {
                                System.out.println("Inseriu a data superior com o formato errado");
                            }
                        } catch (DateTimeParseException dtpe){
                            System.out.println("Inseriu a data inferior com o formato errado");
                        }
                        //MenuInput m2 = new MenuInput("Insira a destino da sua viagem:", "Destino:");
                        m1.executa();
                        m2.executa();
                    }
                 /*
                case 5:
                    //Menus de origem, destino, e data inicial e final
                    String origem = "";
                    List<String> dest = new ArrayList<>();
                    LocalDate inDate = LocalDate.now(), finDate = LocalDate.now();

                    t = new Cliente.Thread5(origem, dest, inDate, finDate);
                    break;
                case 6:
                    t = new Cliente.Thread6();
                    break;
                case 7:
                    t = new Cliente.Thread7();
                    break;
                case 8:
                    MenuInput m8 = new MenuInput("Insira o codigo de reserva:", "Cod Reserva:");
                    t = new Cliente.Thread8(m8.getOpcao());
                    break;
                case 9:
                    t = new Cliente.Thread9();
                    break;
                default:
                    flag = true;
                    break;

                     */
                }
                if (flag==-2) System.out.println("Falha de conexão");
            }catch (ServerIsClosedException e){
                System.out.println("O Servidor encontra-se fechado, tente novamente mais tarde");
            }
        }
    }
}
