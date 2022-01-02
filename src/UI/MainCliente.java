package UI;

import DataLayer.Reservas;
import Demultiplexer.Cliente;
import Demultiplexer.Demultiplexer;
import Demultiplexer.TaggedConnection;
import Demultiplexer.Viagens;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MainCliente {
    /**
     * Classe utilizada para sinalizacao de diversos eventos.
     * Os seguintes valores estao reservados:
     *      -> -404 : Servidor esta fechado
     *      -> -400 : Fechar cliente
     *      ->   -1 : Nao autenticado
     *      ->    0 : Cliente autenticado
     *      ->    1 : Administrador autenticado
     */
    public static class Flag{
        private Integer flag;
        public static final int SERVER_CLOSED     = -404;
        public static final int CLOSE_CLIENT      = -400;
        public static final int NOT_AUTHENTICATED =   -1;
        public static final int CLIENT_LOGGED_IN  =    0;
        public static final int ADMIN_LOGGED_IN   =    1;

        public Flag(){ flag = NOT_AUTHENTICATED; }
        private ReentrantLock lock = new ReentrantLock();
        public Integer getValue(){ try{ lock.lock(); return flag; } finally{ lock.unlock();} }
        public void setValue(Integer flag){ try{ lock.lock(); this.flag = flag; } finally{ lock.unlock();} }
    }


    //Lock para controlar concorrencia nos prints
    private static final ReentrantLock printsLock = new ReentrantLock(true);

    public static void main(String[] args) {
        Socket s;
        Demultiplexer m;

        try {
            s = new Socket("localhost", 8888);
            s.setSoTimeout(100);
            m = new Demultiplexer(new TaggedConnection(s));
            m.start();
        }catch (UnknownHostException uhe){
            System.out.println("Servidor offline.");
            return;
        }catch (IOException ioe){
            System.out.println("Problema com o socket utilizado para a conexão.");
            return;
        }

        Flag flag              = new Flag();
        Cliente cliente        = new Cliente(m);
        AtomicInteger nrPedido = new AtomicInteger(0);

        //Menu de autenticacao
        Menu menuAutenticaco = new Menu("Menu de autenticacao", new String[]{"Registar cliente", "Registar adminstrador", "Autenticar"});
        menuAutenticaco.setHandlerSaida(() -> flag.setValue(Flag.CLOSE_CLIENT));
        menuAutenticaco.setHandler(1, () -> registarClienteHandler(nrPedido, cliente));
        menuAutenticaco.setHandler(2, () -> registarAdminHandler(nrPedido, cliente));
        menuAutenticaco.setHandler(3, () -> autenticarHandler(nrPedido, flag, cliente));

        //Menu de cliente
        Menu menuCliente = new Menu("Cliente", new String[]{"Reservar Viagem", "Cancelar Reserva de Viagem", "Listar Voos", "Listar Viagens", "Listar Viagens a partir de uma Origem ate um Destino", "Receber resposta de pedidos"});
        menuCliente.setHandlerSaida(() -> flag.setValue(Flag.NOT_AUTHENTICATED));
        menuCliente.setHandler(1, () -> reservarViagemHandler(nrPedido, cliente));
        menuCliente.setHandler(2, () -> cancelarReservaHandler(nrPedido, cliente));
        menuCliente.setHandler(3, () -> listarVoosHandler(nrPedido, cliente));
        menuCliente.setHandler(4, () -> listarViagensHandler(nrPedido, cliente));
        menuCliente.setHandler(5, () -> listarViagensRestritasHandler(nrPedido, cliente));
        menuCliente.setHandler(6, () -> {});

        //Menu de administrador
        Menu menuAdmin = new Menu("Administrador", new String[]{"Executar Operacoes de Cliente", "Inserir Novo Voo", "Encerrar um Dia","Fechar servidor"});
        menuAdmin.setHandlerSaida(() -> flag.setValue(Flag.NOT_AUTHENTICATED));
        menuAdmin.setHandler(1, () -> { while (runMenuOneTime(menuCliente) != 0); });
        menuAdmin.setHandler(2, () -> inserirNovoVooHandler(nrPedido, cliente));
        menuAdmin.setHandler(3, () -> encerrarDiaHandler(nrPedido, flag,cliente));
        menuAdmin.setHandler(4, () -> fecharServidorHandler(nrPedido, cliente));

        while (!flag.getValue().equals(Flag.CLOSE_CLIENT)) {

            //Executa menu de autenticacao
            while (flag.getValue().equals(Flag.NOT_AUTHENTICATED))
                runMenuOneTime(menuAutenticaco);

            if (flag.getValue().equals(Flag.CLIENT_LOGGED_IN))
                while (runMenuOneTime(menuCliente) != 0);

            else if (flag.getValue().equals(Flag.ADMIN_LOGGED_IN))
                while (runMenuOneTime(menuAdmin) != 0);

            if(flag.getValue().equals(Flag.SERVER_CLOSED)) {
                flag.setValue(Flag.NOT_AUTHENTICATED);
                printRespostaPedido("O Servidor encontra-se fechado. Tente novamente mais tarde!", null);
            }
        }

        m.close();
    }

    // ****** Handlers Autenticacao ****** //

    private static void registarClienteHandler(AtomicInteger nrPedido, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();

        try {
            printsLock.lock();

            MenuInput m1 = new MenuInput("Insira um username", "Username:");
            MenuInput m2 = new MenuInput("Insira uma password", "Password:");
            m1.executa();
            m2.executa();

            String headerPedido = "Criacao de conta Cliente";

            int flagInterna = cliente.criaConta(nr, m1.getOpcao(), m2.getOpcao(), false);
            if (flagInterna == 0) printRespostaPedido(headerPedido, "Cliente criado com sucesso");
            else if (flagInterna == 1) printRespostaPedido(headerPedido, "Falha ao criar cliente");
            else if (flagInterna == -2) printRespostaPedido(headerPedido, "Erro de conexão. Tente novamente. Se o problema persistir o servidor pode estar offline.");

        }finally { printsLock.unlock(); }
    }

    private static void registarAdminHandler(AtomicInteger nrPedido, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();

        try {
            printsLock.lock();

            MenuInput m1 = new MenuInput("Insira um username", "Username:");
            MenuInput m2 = new MenuInput("Insira uma password", "Password:");
            m1.executa();
            m2.executa();

            String headerPedido = "Criacao de conta Admin";

            int flagInterna = cliente.criaConta(nr, m1.getOpcao(), m2.getOpcao(), true);
            if (flagInterna == 0) printRespostaPedido(headerPedido, "Administrador criado com sucesso");
            else if (flagInterna == 1) printRespostaPedido(headerPedido, "Falha ao criar administrador");
            else if (flagInterna == -2) printRespostaPedido(headerPedido, "Erro de conexão. Tente novamente. Se o problema persistir o servidor pode estar offline.");

        } finally { printsLock.unlock(); }
    }

    private static void autenticarHandler(AtomicInteger nrPedido, Flag flag, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();

        try {
            printsLock.lock();

            MenuInput m1 = new MenuInput("Insira o seu username", "Username:");
            MenuInput m2 = new MenuInput("Insira o seu password", "Password:");
            m1.executa();
            m2.executa();

            String headerPedido = "Autenticacao";

            int flagInterna = cliente.login(nr, m1.getOpcao(), m2.getOpcao());
            if (flagInterna == -1) printRespostaPedido(headerPedido, "Falha no login");
            else if (flagInterna == 0) {
                printRespostaPedido(headerPedido, "Cliente logado com sucesso");
                flag.setValue(Flag.CLIENT_LOGGED_IN);
            } else if (flagInterna == 1) {
                printRespostaPedido(headerPedido, "Administrador logado com sucesso");
                flag.setValue(Flag.ADMIN_LOGGED_IN);
            } else if (flagInterna == -2)
                printRespostaPedido(headerPedido, "Erro de conexão. Tente novamente. Se o problema persistir o servidor pode estar offline.");

        }finally { printsLock.unlock(); }
    }

    // ****** Handlers Cliente ****** //

    private static void reservarViagemHandler(AtomicInteger nrPedido, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();

        LocalDate dataInicial = null, dataFinal = null;
        List<String> locais = new ArrayList<>();

        try {
            printsLock.lock();

            MenuInput m = new MenuInput("Insira o numero de localizacoes que pretende inserir", "Numero: ");
            MenuInput m1 = new MenuInput("Insira uma localizacao", "Localizacao:");
            MenuInput m2 = new MenuInput("Insira a data inicial (com o formato \"YYYY-MM-DD\")", "Data: ");
            MenuInput m3 = new MenuInput("Insira a data final (com o formato \"YYYY-MM-DD\")", "Data: ");

            Integer nrLocais = null;

            while (nrLocais == null) {
                try {
                    m.executa();
                    nrLocais = Integer.parseInt(m.getOpcao());
                } catch (NumberFormatException nfe) {
                    System.out.println("Por favor insira um numero inteiro.");
                }
            }

            System.out.println("Insira as localizacoes pela ordem que pretende executar o percurso.\n");
            for (int i = 0; i < nrLocais; i++) {
                m1.executa();
                locais.add(m1.getOpcao());
            }

            System.out.println("Insira o intervalo de datas pretendido para a reserva.\n");


            while (dataInicial == null) {
                try {
                    m2.executa();
                    dataInicial = LocalDate.parse(m2.getOpcao());
                } catch (DateTimeParseException dtpe) {
                    System.out.println("Formato errado!");
                }
            }

            while (dataFinal == null) {
                try {
                    m3.executa();
                    dataFinal = LocalDate.parse(m3.getOpcao());
                } catch (DateTimeParseException dtpe) {
                    System.out.println("Formato errado!");
                }
            }

        }finally { printsLock.unlock(); }

        final LocalDate dataInicialF = dataInicial;
        final LocalDate dataFinalF   = dataFinal;
        new Thread(() -> {
            int internaFlag = cliente.fazReserva(nr, locais, dataInicialF, dataFinalF);
            if (internaFlag >= 0)
                System.out.println("ID da reserva: " + internaFlag);
            else if (internaFlag == -1)
                System.out.println("Falha na verificacao de seguranca");
            else if (internaFlag == -2)
                System.out.println("Localizacoes inseridas são inválidas.");
            else if (internaFlag == -3)
                System.out.println("Numero de localizacoes inserido é invalido.");
            else if (internaFlag == -4)
                System.out.println("Utilizador ja possui uma reserva para este dia ou nao existem lugares disponiveis.");
            else System.out.println("Error");
        }).start();
    }

    private static void cancelarReservaHandler(AtomicInteger nrPedido, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();

        Integer idReserva = null;

        try {
            printsLock.lock();

            MenuInput m = new MenuInput("Insira o id da sua reserva", "ID:");

            while (idReserva == null) {
                try {
                    m.executa();
                    idReserva = Integer.valueOf(m.getOpcao());
                } catch (NumberFormatException ex) {
                    System.out.println("ID de reserva tem que ser um numero inteiro");
                }
            }

        } finally { printsLock.unlock(); }


        final int idReservaFinal = idReserva;
        new Thread(() -> {

            int flagInterna = cliente.cancelaReserva(nr, idReservaFinal);

            String headerPedido = "Cancelamento da Reserva (ID: " + idReservaFinal + ")";

            if (flagInterna == 0) printRespostaPedido(headerPedido, "Reserva removida");
            else if (flagInterna == 1) printRespostaPedido(headerPedido, "ID da reserva nao foi encontrado");
            else if (flagInterna == 2)
                printRespostaPedido(headerPedido, "Falha de seguranca, tente sair da conta e voltar a fazer login");
            else if (flagInterna == -2)
                printRespostaPedido(headerPedido, "Erro de conexão. Tente novamente. Se o problema persistir o servidor pode estar offline.");

        }).start();
    }

    private static void listarVoosHandler(AtomicInteger nrPedido, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();

        try {
            printsLock.lock();
            System.out.println("A carregar voos possiveis");
        }
        finally { printsLock.unlock(); }

        new Thread(() -> {
            List<List<String>> viagens = cliente.listaVoosPossiveis(nr);

            String headerPedido = "Listagem De Todos Os Voos Possiveis";

            if (viagens == null) printRespostaPedido(headerPedido, "Falha de conexao");
            else if (viagens.size() == 0) printRespostaPedido(headerPedido, "Nao existem voos possiveis");
            else printRespostaPedido(headerPedido, Viagens.toStringOutput(viagens));
        }).start();
    }

    private static void listarViagensHandler(AtomicInteger nrPedido, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();

        printRespostaPedido("A carregar viagens possiveis", null);

        new Thread(() -> {

            List<List<String>> viagens = cliente.listaViagensEscalasSimples(nr);

            String headerPedido = "Listagem De Todas As Viagens Possiveis";

            if (viagens == null) printRespostaPedido(headerPedido, "Falha de conexao");
            else if (viagens.size() == 0) printRespostaPedido(headerPedido, "Nao existem voos possiveis");
            else printRespostaPedido(headerPedido, Viagens.toStringOutput(viagens));

        }).start();
    }

    private static void listarViagensRestritasHandler(AtomicInteger nrPedido, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();
        String origem, destino;

        try {
            printsLock.lock();

            MenuInput m1 = new MenuInput("Insira a origem da sua viagem", "Origem:");
            MenuInput m2 = new MenuInput("Insira a destino da sua viagem", "Destino:");
            m1.executa();
            m2.executa();

            origem = m1.getOpcao();
            destino = m2.getOpcao();

            System.out.println("A carregar viagens possiveis entre" + origem + " e " + destino + ".");
        } finally { printsLock.unlock(); }

        new Thread(() -> {

            List<List<String>> viagens = cliente.listaViagensEscalas(nr, origem, destino);

            String headerPedido = "Listagem De Todas As Viagens Possiveis entre" + origem + " e " + destino ;

            if (viagens == null) printRespostaPedido(headerPedido, "Falha de conexao");
            else if (viagens.size() == 0) printRespostaPedido(headerPedido, "Nao existem viagens para estes destinos");
            else printRespostaPedido(headerPedido, Viagens.toStringOutput(viagens, origem, destino));

        }).start();
    }

    // ****** Handlers Admin ****** //

    private static void inserirNovoVooHandler(AtomicInteger nrPedido, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();

        try {
            printsLock.lock();

            MenuInput m1 = new MenuInput("Insira a origem", "Origem:");
            MenuInput m2 = new MenuInput("Insira o destino", "Destino:");
            MenuInput m3 = new MenuInput("Insira a capacidade", "Capacidade:");
            m1.executa();
            m2.executa();
            boolean dummyflag;
            int n = 0;
            do {
                dummyflag = false;
                m3.executa();
                try {
                    n = Integer.parseInt(m3.getOpcao());

                } catch (NumberFormatException e) {
                    dummyflag = true;
                }
            } while (dummyflag);

            int flagInterna = cliente.addVoo(nr, m1.getOpcao(), m2.getOpcao(), n);
            if (flagInterna == 0) System.out.println("Voo adicionado com sucesso");
            else if (flagInterna == 1) System.out.println("Falha ao adicionar voo");
            else if (flagInterna == -2) System.out.println("Erro de conexão. Tente novamente. Se o problema persistir o servidor pode estar offline.");

        } finally { printsLock.unlock(); }
    }

    private static void encerrarDiaHandler(AtomicInteger nrPedido, Flag flag, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();

        try {
            printsLock.lock();

            MenuInput m = new MenuInput("Insira a data com o seguinte formato \"YYYY-MM-DD\"", "Data:");
            m.executa();

            LocalDate opcao = null;

            while (opcao == null) {
                try {
                    opcao = LocalDate.parse(m.getOpcao());
                } catch (DateTimeParseException dtpe) {
                    System.out.println("Inseriu uma data com o formato errado");
                }
            }

            int flagInterna = cliente.encerraDia(nr, opcao);
            if (flagInterna == 0) System.out.println("Dia fechado com sucesso");
            else if (flagInterna == 1) System.out.println("Falha fechar o dia");
            else if (flagInterna == -2)
                System.out.println("Erro de conexão. Tente novamente. Se o problema persistir, o servidor pode estar offline.");

        } finally { printsLock.unlock(); }
    }

    private static void fecharServidorHandler(AtomicInteger nrPedido, Cliente cliente) {
        //Atualizacao do número de pedido
        int nr = nrPedido.getAndIncrement();
        cliente.fecharServidor(nr);
    }


    // Metodos auxiliares dos prints que controlam a concorrência
    private static int runMenuOneTime(Menu menu){
        /*try { Thread.sleep(50); }
        catch (InterruptedException ignored) {}*/

        try {
            printsLock.lock();
            menu.runOneTime();
            return menu.getLastOption();
        } finally { printsLock.unlock(); }
    }

    private static void printRespostaPedido( String header, String resposta){
        try {
            printsLock.lock();
            System.out.println("\n\n******************** " + header + " ********************\n");
            if(resposta != null) {
                System.out.println(resposta);
                char[] arr = new char[header.length()];
                Arrays.fill(arr, '*');
                System.out.println("\n******************************************" + String.valueOf(arr) + "\n\n");
            }
        } finally { printsLock.unlock(); }
    }
}