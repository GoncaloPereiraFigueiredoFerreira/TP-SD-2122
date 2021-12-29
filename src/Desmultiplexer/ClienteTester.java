package Desmultiplexer;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClienteTester {
    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        Scanner sc = new Scanner(System.in);

        int flag=0;
        while (flag!=-1) {

            System.out.println("Ensira a proxima operacao");
            int option = sc.nextInt();
            Thread t = null;

            //Need to know the opcode
            switch (option) {
                case 0 -> flag=cliente.fecharServidor();
                case 1 -> {
                    MenuInput m1 = new MenuInput("Insira o seu username:", "Username:");
                    MenuInput m2 = new MenuInput("Insira o seu password:", "Password:");
                    m1.executa();
                    m2.executa();
                    flag = cliente.criaConta(m1.getOpcao(), m2.getOpcao());
                    if(flag==0) System.out.println("Cliente criado com sucesso");
                    if(flag==1) System.out.println("Falha ao criar cliente");
                }
                case 2 -> {
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
                    cliente.addVoo(m5.getOpcao(), m6.getOpcao(), n);
                }
                    /*
                case 4:
                    //Menu de datas
                    LocalDate l = LocalDate.now();
                    t = new Cliente.Thread4(l);
                    break;
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
            //if (!flag) t.start();
        }
        if(flag==-1) System.out.println("O Servidor encontra-se fechado, tente novamente mais tarde");
    }
}
