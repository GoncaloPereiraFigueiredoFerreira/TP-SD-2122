package UI;

import Demultiplexer.Cliente;
import Demultiplexer.Exceptions.ServerIsClosedException;
import Demultiplexer.Viagens;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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

    public static void main(String[] args) {
        Flag flag = new Flag();
        Cliente cliente = new Cliente();

        //Menu de autenticacao
        Menu menuAutenticaco = new Menu("Menu de autenticacao", new String[]{"Registar cliente", "Registar adminstrador", "Autenticar"});
        menuAutenticaco.setHandlerSaida(() -> flag.setValue(Flag.CLOSE_CLIENT));
        menuAutenticaco.setHandler(1, () -> registarClienteHandler(flag, cliente));
        menuAutenticaco.setHandler(2, () -> registarAdminHandler(flag, cliente));
        menuAutenticaco.setHandler(3, () -> autenticarHandler(flag, cliente));

        //Menu de cliente
        Menu menuCliente = new Menu("Cliente", new String[]{"Reservar Viagem", "Cancelar Reserva de Viagem", "Listar Voos", "Listar Viagens", "Listar Viagens a partir de uma Origem ate um Destino"});
        menuCliente.setHandlerSaida(() -> flag.setValue(Flag.NOT_AUTHENTICATED));
        menuCliente.setHandler(1, () -> reservarViagemHandler(flag, cliente));
        menuCliente.setHandler(2, () -> cancelarReservaHandler(flag, cliente));
        menuCliente.setHandler(3, () -> listarVoosHandler(flag, cliente));
        //TODO - falta handler 4
        menuCliente.setHandler(5, () -> listarViagensRestritasHandler(flag, cliente));

        //Menu de administrador
        Menu menuAdmin = new Menu("Administrador", new String[]{"Executar Operacões de Cliente", "Inserir Novo Voo", "Encerrar um Dia"});
        menuAdmin.setHandlerSaida(() -> flag.setValue(Flag.NOT_AUTHENTICATED));
        menuAdmin.setHandler(1, menuCliente::run);
        menuAdmin.setHandler(2, () -> inserirNovoVooHandler(flag, cliente));
        menuAdmin.setHandler(3, () -> encerrarDiaHandler(flag,cliente));

        while (!flag.getValue().equals(Flag.CLOSE_CLIENT)) {

            //Executa menu de autenticacao
            while (flag.getValue().equals(Flag.NOT_AUTHENTICATED))
                menuAutenticaco.runOneTime();

            if (flag.getValue().equals(Flag.CLIENT_LOGGED_IN))
                menuCliente.run();
            else if (flag.getValue().equals(Flag.ADMIN_LOGGED_IN))
                menuAdmin.run();

            if(flag.getValue().equals(Flag.SERVER_CLOSED)) {
                flag.setValue(Flag.NOT_AUTHENTICATED);
                System.out.println("O Servidor encontra-se fechado. Tente novamente mais tarde!");
            }
        }
    }

    // ****** Handlers Autenticacao ****** //

    private static void registarClienteHandler(Flag flag, Cliente cliente){
        MenuInput m1 = new MenuInput("Insira um username", "Username:");
        MenuInput m2 = new MenuInput("Insira uma password", "Password:");
        m1.executa();
        m2.executa();

        try {
            int flagInterna = cliente.criaConta(m1.getOpcao(), m2.getOpcao(), false);
            if (flagInterna == 0) System.out.println("Cliente criado com sucesso");
            else if (flagInterna == 1) System.out.println("Falha ao criar cliente");
        } catch (ServerIsClosedException sice) {
            flag.setValue(Flag.SERVER_CLOSED);
        }
    }

    private static void registarAdminHandler(Flag flag, Cliente cliente) {
        MenuInput m1 = new MenuInput("Insira um username:", "Username:");
        MenuInput m2 = new MenuInput("Insira uma password:", "Password:");
        m1.executa();
        m2.executa();

        try {
            int flagInterna = cliente.criaConta(m1.getOpcao(), m2.getOpcao(),true);
            if (flagInterna == 0) System.out.println("Aministrador criado com sucesso");
            else if (flagInterna == 1) System.out.println("Falha ao criar administrador");
        } catch (ServerIsClosedException sice) {
            flag.setValue(Flag.SERVER_CLOSED);
        }
    }

    private static void autenticarHandler(Flag flag, Cliente cliente) {
        MenuInput m1 = new MenuInput("Insira o seu username:", "Username:");
        MenuInput m2 = new MenuInput("Insira o seu password:", "Password:");
        m1.executa();
        m2.executa();

        try {
            int flagInterna = cliente.login(m1.getOpcao(), m2.getOpcao());
            if (flagInterna == -1) System.out.println("Falha no login");
            else if (flagInterna == 0) {
                System.out.println("Cliente logado com sucesso");
                flag.setValue(Flag.CLIENT_LOGGED_IN);
            }
            else if (flagInterna == 1){
                System.out.println("Administrador logado com sucesso");
                flag.setValue(Flag.ADMIN_LOGGED_IN);
            }
        } catch (ServerIsClosedException sice) {
            flag.setValue(Flag.SERVER_CLOSED);
        }
    }

    // ****** Handlers Cliente ****** //

    private static void reservarViagemHandler(Flag flag, Cliente cliente) {
        MenuInput m  = new MenuInput("Insira o numero de localizacões que pretende inserir:","Numero: ");
        MenuInput m1 = new MenuInput("Insira uma localizacao:", "Localizacao:" ); //todo alterar localizacoes
        MenuInput m2 = new MenuInput("Insira a data inicial (com o formato \"YYYY-MM-DD\"):", "Data: ");
        MenuInput m3 = new MenuInput("Insira a data final (com o formato \"YYYY-MM-DD\"):", "Data: ");

        Integer nrLocais = null;

        while (nrLocais == null) {
            try {
                m.executa();
                nrLocais = Integer.parseInt(m.getOpcao());
            } catch (NumberFormatException nfe) {
                System.out.println("Por favor insira um numero inteiro.");
            }
        }

        System.out.println("Insira as localizacões pela ordem que pretende executar o percurso.\n");
        List<String> locais = new ArrayList<>();
        for (int i = 0; i < nrLocais; i++){
            m1.executa();
            locais.add(m1.getOpcao());
        }

        System.out.println("Insira o intervalo de datas pretendido para a reserva.\n");


        LocalDate dataInicial = null, dataFinal = null;

        while (dataInicial == null){
            try {
                m2.executa();
                dataInicial = LocalDate.parse(m2.getOpcao());
            } catch (DateTimeParseException dtpe){
                System.out.println("Formato errado!");
            }
        }

        while (dataFinal == null){
            try {
                m3.executa();
                dataFinal = LocalDate.parse(m3.getOpcao());
            } catch (DateTimeParseException dtpe){
                System.out.println("Formato errado!");
            }
        }

        try {
            
            cliente.fazReserva(locais, dataInicial, dataFinal);

        }catch (ServerIsClosedException sice){
            flag.setValue(Flag.SERVER_CLOSED);
        }
    }

    private static void cancelarReservaHandler(Flag flag, Cliente cliente) {
        MenuInput m = new MenuInput("Insira o id da sua reserva", "ID:");
        //Menu de datas
        Integer idReserva=null;

        while (idReserva==null) {
            try {
                m.executa();
                idReserva = Integer.valueOf(m.getOpcao());
            } catch (NumberFormatException ex) {
                System.out.println("ID de reserva tem que ser um numero inteiro");
            }
        }

        try {
            int flagInterna = cliente.cancelaReserva(idReserva);
            if (flagInterna == 0) System.out.println("Reserva removida");
            else if (flagInterna == 1) System.out.println("ID da reserva nao foi encontrado");
            else if (flagInterna == 2) System.out.println("Falha de seguranca, tente sair da conta e voltar a fazer login");
        } catch (ServerIsClosedException sice) {
            flag.setValue(Flag.SERVER_CLOSED);
        }
    }

    private static void listarVoosHandler(Flag flag, Cliente cliente) {
        System.out.println("A carregar voos possiveis");

        try {
            List<List<String>> viagens = cliente.listaVoosPossiveis();

            if (viagens == null) System.out.println("Falha de conexao");
            else if (viagens.size() == 0) System.out.println("Nao existem voos possiveis");
            else System.out.println(Viagens.toStringOutput(viagens));
        }
        catch (ServerIsClosedException sice){
            flag.setValue(Flag.SERVER_CLOSED);
        }
    }

    //TODO - falta handler 4

    private static void listarViagensRestritasHandler(Flag flag, Cliente cliente) {
        MenuInput m1 = new MenuInput("Insira a origem da sua viagem:", "Origem:");
        MenuInput m2 = new MenuInput("Insira a destino da sua viagem:", "Destino:");
        m1.executa();
        m2.executa();
        //Menu de datas
        String origem = m1.getOpcao();
        String destino = m2.getOpcao();

        try {
            List<List<String>> viagens = cliente.listaViagensEscalas(origem, destino);
            if (viagens == null) System.out.println("Falha de conexao");
            else if (viagens.size() == 0) System.out.println("Nao existem viagens para estes destinos");
            else System.out.println(Viagens.toStringOutput(viagens, origem, destino));
        } catch (ServerIsClosedException sice){
            flag.setValue(Flag.SERVER_CLOSED);
        }
    }

    // ****** Handlers Admin ****** //

    private static void inserirNovoVooHandler(Flag flag, Cliente cliente) {
        MenuInput m1 = new MenuInput("Insira a origem:", "Origem:");
        MenuInput m2 = new MenuInput("Insira o destino:", "Destino:");
        MenuInput m3 = new MenuInput("Insira a capacidade:", "Capacidade:");
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

        try {
            int flagInterna = cliente.addVoo(m1.getOpcao(), m2.getOpcao(), n);
            if (flagInterna == 0) System.out.println("Voo adicionado com sucesso");
            else if (flagInterna == 1) System.out.println("Falha ao adicionar voo");
        } catch (ServerIsClosedException sice) {
            flag.setValue(Flag.SERVER_CLOSED);
        }

    }

    private static void encerrarDiaHandler(Flag flag, Cliente cliente) {
        MenuInput m = new MenuInput("Insira a data com o seguinte formato \"YYYY-MM-DD\":", "Data:");
        m.executa();

        try {
            LocalDate opcao = null;

            while (opcao == null) {
                try {
                    opcao = LocalDate.parse(m.getOpcao());
                } catch (DateTimeParseException dtpe) {
                    System.out.println("Inseriu uma data com o formato errado");
                }
            }

            int flagInterna = cliente.encerraDia(opcao);
            if (flagInterna == 0) System.out.println("Dia fechado com sucesso");
            else if (flagInterna == 1) System.out.println("Falha fechar o dia");

        } catch (ServerIsClosedException sice){
            flag.setValue(Flag.SERVER_CLOSED);
        }
    }
}