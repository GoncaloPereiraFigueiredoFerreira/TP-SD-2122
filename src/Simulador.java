import DataLayer.InformacaoSobreReserva;
import LogicLayer.Cliente.Cliente;
import LogicLayer.Cliente.Demultiplexer;
import LogicLayer.Cliente.Exceptions.ReservaFailException;
import LogicLayer.Cliente.Exceptions.ServerIsClosedException;
import LogicLayer.Servidor.Operacoes.*;
import LogicLayer.Servidor.Server;
import LogicLayer.TaggedConnection;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Simulador {

    static class ClienteRunner implements Runnable{
        List<String> locais;
        int nrLocais;
        String endereco;
        List<Integer> idsResevas = new ArrayList<>();
        int nrPedidosAExecutar;
        int nrCliente;

        public ClienteRunner(int nrCliente, List<String> locais, String endereco, int nrPedidosAExecutar){
            this.nrCliente = nrCliente;
            this.locais    = locais;
            this.nrLocais  = locais.size();
            this.endereco  = endereco;
            this.nrPedidosAExecutar = nrPedidosAExecutar;
        }

        @Override
        public void run() {
            Socket s;
            Demultiplexer m;
            Cliente cliente;
            int nrPedido = 0;

            //Inicializacao do cliente
            try {
                s = new Socket(endereco, 8888);
                s.setSoTimeout(100);
                m = new Demultiplexer(new TaggedConnection(s));
                m.start();
                cliente = new Cliente(m);
            } catch (UnknownHostException uhe) {
                System.out.println("Servidor offline.");
                return;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("Problema com o socket utilizado para a conexao.");
                return;
            }

            //Criacao de conta e login
            String currentThreadName = Thread.currentThread().getName();
            try {
                if(cliente.criaConta(nrPedido++, currentThreadName, currentThreadName, false) != 0 || cliente.login(nrPedido++, currentThreadName, currentThreadName) != 0){
                    try {
                        m.close();
                        s.close();
                    } catch (IOException ignored) {}
                    return;
                }
            } catch (ServerIsClosedException e) {
                return;
            }

            Random random       = new Random();
            LocalDate dataAtual = LocalDate.now();

            //Faz reservas aleatórias e guarda o id das que conseguiu executar
            for(int i = 0; i < nrPedidosAExecutar; i++){

                //Escolhe 2 a 4 localizacoes
                int nrLocaisRand        = random.nextInt(3) + 2;
                List<String> locaisRand = new ArrayList<>();
                for(int j = 0; j < nrLocaisRand; j++){
                    String local;

                    do { local = locais.get(random.nextInt(nrLocais)); }
                    while (locaisRand.contains(local));

                    locaisRand.add(local);
                }

                //Escolhe um intervalo de datas
                LocalDate dataInicial = dataAtual.plusDays(random.nextInt(31));
                LocalDate dataFinal   = dataInicial.plusDays(random.nextInt(15));

                //new Thread(() -> {
                    try {
                        InformacaoSobreReserva reserva = cliente.fazReserva(nrPedido, locaisRand, dataInicial, dataFinal);
                        idsResevas.add(reserva.getIdReserva());
                    }catch (Exception ignored){}
                //}).start();

                nrPedido++;
            }

            System.out.println("Reservas do Cliente #" + nrCliente + ": " + idsResevas); System.out.flush();

            //Elimina Reservas efetuadas
            for(Integer id : idsResevas) {
                try {
                    cliente.cancelaReserva(nrPedido++, id);
                } catch (Exception ignored) {}
            }

            //Verificacao se coincide com os dados armazenados no servidor
            try {
                System.out.println("Reservas (no servidor) do Cliente #" + nrCliente + ": " + cliente.listaReservasUtilizador(nrPedido++).stream().map(InformacaoSobreReserva::getIdReserva).collect(Collectors.toList()));
                System.out.flush();
            } catch (ServerIsClosedException e) { return; }

            try {
                m.close();
                s.close();
            } catch (IOException ignored) {}
        }

    }

    public static int runAdmin(Socket s, Demultiplexer m, Cliente cliente, List<String> locais) {
        int nrPedido = 0;

        //Criacao de conta e login
        String currentThreadName = Thread.currentThread().getName();
        try {
            if(cliente.criaConta(nrPedido++, currentThreadName, currentThreadName, true) != 0 || cliente.login(nrPedido++, currentThreadName, currentThreadName) != 1){
                try {
                    m.close();
                    s.close();
                } catch (IOException ignored) {}
                return -1;
            }
        } catch (ServerIsClosedException e) {
            return -1;
        }

        Random random = new Random();

        //Criacao de voos
        for(int i = 0; i < locais.size(); i++){
            String origem = locais.get(i);
            for(int j = 0; j < locais.size(); j++){
                if(j != i) {
                    final int nrPedidoF = nrPedido;
                    String destino = locais.get(j);

                        new Thread( () -> {
                            try {
                            cliente.addVoo(nrPedidoF, origem, destino, random.nextInt(200) + 1);
                            } catch (ServerIsClosedException e) {
                                return;
                            }
                        }).start();

                    nrPedido++;
                }
            }
        }

        return nrPedido;
    }

    public static Server runServer() {
        Server s = new Server();

        List<OperacaoI> operacoes = new ArrayList<>();
        operacoes.add(new CriaConta());
        operacoes.add(new AddVoo());
        operacoes.add(new Login());
        operacoes.add(new EncerraDia());
        operacoes.add(new ListaViagensEscalas());
        operacoes.add(new AdicionaReserva());
        operacoes.add(new ListaVoosPossiveis());
        operacoes.add(new CancelaReserva());
        operacoes.add(new ListaViagensEscalasSimples());
        operacoes.add(new ReservasUtilizador());

        if(!s.loadServer(operacoes)){
            System.out.println("Erro ao dar load ao servidor");
            return null;
        }
        System.out.println("Server is running");
        s.start();

        return s;
    }

    public static void main(String[] args) throws InterruptedException {
        //Inputs
        int nrTentativasReservaPorCliente = 10;
        int nrClientes = 1000;
        List<String> locais = Arrays.asList("Porto","Tokyo","NewYork","Lisboa","Madrid","Barcelona","Paris");
        String endereco = "localhost";

        //Inicializacao Server
        Server sv = runServer();

        //Inicializacao Cliente administrador
        Socket s;
        Demultiplexer m;
        Cliente cliente;
        int nrPedido;
        try {
            s = new Socket(endereco, 8888);
            s.setSoTimeout(100);
            m = new Demultiplexer(new TaggedConnection(s));
            m.start();
            cliente = new Cliente(m);
        } catch (UnknownHostException uhe) {
            System.out.println("Servidor offline.");
            return;
        } catch (IOException ioe) {
            System.out.println("Problema com o socket utilizado para a conexao.");
            return;
        }
        if((nrPedido = runAdmin(s, m, cliente, locais)) == -1) return;
        Thread.sleep(500);  //Tempo para acabar de adicionar todos os voos

        //Listagem dos voos
        try {
            List<List<String>> listaVoos = cliente.listaVoosPossiveis(nrPedido);
            System.out.println("Listagem voos:\n\n" + listaVoos + "\nNr voos possiveis = " + listaVoos.size());
            System.out.flush();
        }catch (Exception exception) {return;}


        System.out.println("Escreva algo para prosseguir");
        new Scanner(System.in).next();
        System.out.println("A começar"); System.out.flush();

        //Criacao de todos os clientes
        Thread[] clientes = new Thread[nrClientes];
        for(int i = 0; i < nrClientes; i++)
            clientes[i] = new Thread(new ClienteRunner(i,locais,endereco,nrTentativasReservaPorCliente));
        for(int i = 0; i < nrClientes; i++) {
            clientes[i].start();
            if(i % 50 == 0) Thread.sleep(500); //TODO - tirar
        }
        for(int i = 0; i < nrClientes; i++)
            clientes[i].join();

        //Fecha servidor
        try {
            cliente.fecharServidor(nrPedido);
            m.close();
            s.close();
        } catch (Exception ignored) {}

        System.out.println("Já acabaram (supostamente)"); System.out.flush();
    }

}
