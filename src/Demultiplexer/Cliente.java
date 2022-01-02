package Demultiplexer;

import Demultiplexer.ClassesSerializable.Viagens;
import Demultiplexer.Exceptions.ServerIsClosedException;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

public class Cliente {
    private Demultiplexer m;
    private String utilizador;
    private String password;

    public Cliente(Demultiplexer m){ this.m = m; }

    //Deviamos ter operaçao para saber quais as suas reservas
    //Deviamos retornar a data na reserva

    /**
     * Frame recebido com a resposta ao pedido do cliente
     * @param  f Frame recebido que foi enviado pelo servidor
     * @return Resposta ao pedido do cliente
     */
    private int confirmacao(Frame f) throws IOException {
        int rValue;

        ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
        ObjectInputStream ois = new ObjectInputStream(bais);
        rValue = ois.readInt();
        ois.close();
        bais.close();

        return rValue;
    }

    /**
     * Envia frame com tag -1 para fechar o servidor
     * @param  number Numero correspondente a operacao
     */
    public void fecharServidor(int number) throws ServerIsClosedException { //tag -1
        try {
            // Envia tag -1 para sinalizar fecho
            m.send(number, -1, new byte[0], true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envia os dados necessarios ao servidor para executar a operacao de criar conta
     * @param  number Numero da operacao
     * @param username Identificador do novo nome de utilizador
     * @param password Senha corresponde ao novo utilizador
     * @param admin True caso queira criar um admin, false caso contrario
     * @param tag Identificador da operacao que o servidor para ter que executar
     */
    private void sendDadosCriaConta(int number, String username, String password, Boolean admin, int tag) throws IOException,ServerIsClosedException {
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

    /**
     * Cria uma conta nova no servidor
     * @param  number Numero da operacao
     * @param username Identificador do novo nome de utilizador
     * @param password Senha corresponde ao novo utilizador
     * @param administrador True caso queira criar um admin, false caso contrario
     * @return -2 em caso de erro de conexão, -1 caso o id do utilizador ja esteja em uso, 0 caso tenha sido criado um cliente, 1 caso tenha sido criado um admin
     */
    public int criaConta(int number, String username,String password,Boolean administrador) throws ServerIsClosedException { //tag 0
        try {
            // Recebe uma confirmação de criação de conta
            sendDadosCriaConta(number,username,password,administrador,0);
            int confirm = confirmacao(m.receive(number));
            m.finishedReceivingMessages(number);
            return confirm;
        }  catch (IOException e) { return -2; }
    }

    /**
     * Envia os dados necessarios ao servidor para executar a operacao de fazer login
     * @param  number Numero da operacao
     * @param username Identificador do novo nome de utilizador
     * @param password Senha corresponde ao novo utilizador
     * @param tag Identificador da operacao que o servidor para ter que executar
     */
    private void sendDadosLogin(int number, String username,String password, int tag) throws IOException,ServerIsClosedException {
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

    /**
     * Executa a operacao de login do servidor
     * @param  number Numero da operacao
     * @param username Identificador do novo nome de utilizador
     * @param password Senha corresponde ao novo utilizador
     * @return -2 em caso de erro de conexão;
     * -1 se não existir um utilizador com as credenciais fornecidas;
     * 0 se as credenciais estiverem corretas e pertencerem a um utilizador padrão;
     * 1 se as credenciais estiverem corretas e pertencerem a um administrador;
     */
    public int login(int number, String username,String password) throws ServerIsClosedException  { //tag 1
        try {
            // Envia uma password e um username
            sendDadosLogin(number,username,password,1);
            // Recebe uma confirmação de criação de conta
            int confirmacao = confirmacao(m.receive(number));
            m.finishedReceivingMessages(number);
            if(confirmacao==0||confirmacao==1){
                this.utilizador=username;
                this.password=password;
            }
            return confirmacao;  //0 significa que efetuou login de utilizador, 1 de admin  e -1 em caso de falha
        } catch (IOException e) {
            return -2;
        }
    }

    /**
     * Envia os dados necessarios ao servidor para executar a operacao de adicionar um voo
     * @param  number Numero da operacao
     * @param origem Origem do voo
     * @param destino Destino do voo
     * @param capacidade Capacidade de reservas correspondente ao voo
     * @param tag Identificador da operacao que o servidor para ter que executar
     */
    private void sendDadosAddVoo(int number, String origem,String destino,int capacidade, int tag) throws IOException, ServerIsClosedException {
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

    /**
     * Executa a operacao de adicionar um voo no servidor
     * @param  number Numero da operacao
     * @param origem Origem do voo
     * @param destino Destino do voo
     * @param capacidade Capacidade de reservas correspondente ao voo
     * @return -2 em caso de erro de conexão;
     * 0 se o voo foi adicionado com sucesso;
     * 1 se tenha havido um erro ao criar o voo;
     */
    public int addVoo(int number, String origem,String destino,int capacidade) throws ServerIsClosedException { // tag 2
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

    /**
     * Envia os dados necessarios ao servidor para executar a operacao de encerrar um dia
     * @param  number Numero da operacao
     * @param dia Dia em que pretende encerrar a adicao de reservas
     * @param tag Identificador da operacao que o servidor para ter que executar
     */
    private void sendDadosEncerraDia(int number, LocalDate dia,int tag) throws IOException,ServerIsClosedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(dia);
        oos.flush();

        byte[] byteArray = baos.toByteArray();
        m.send(number, tag, byteArray, false);

        oos.close();
        baos.close();
    }

    /**
     * Executa a operacao no servidor de encerrar um dia
     * @param  number Numero da operacao
     * @param dia Dia em que pretende encerrar a adicao de reservas
     * @return -2 em caso de erro de conexão;
     * 0 se o dia foi fechado com sucesso;
     * 1 se tenha havido um erro ao fechar o dia;
     */
   public int encerraDia(int number, LocalDate dia) throws ServerIsClosedException { // tag 3
       try {
           sendDadosEncerraDia(number,dia,3);
           int confirmacao = confirmacao(m.receive(number));  //0 significa que o dia foi fechado, 1 caso contrário
           m.finishedReceivingMessages(number);
           return confirmacao;
       } catch (IOException e) {
           return 1;
       }
   }

    /**
     * Executa a operacao no servidor de listar Voos possiveis
     * @param  number Numero da operacao
     * @return null em caso de erro de conexão;
     * lista vazia caso não existam voos;
     * lista com voos;
     */
    public List<List<String>> listaVoosPossiveis(int number) throws ServerIsClosedException { //tag 4
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

    /**
     * Envia os dados necessarios ao servidor para executar a operacao de listar viagens com escalas entre duas localizacoes
     * @param  number Numero da operacao
     * @param origem Origem da reserva
     * @param destino Destino da reserva
     * @param tag Identificador da operacao que o servidor para ter que executar
     */
    private void sendDadosViagensEscalas(int number, String origem, String destino, int tag) throws IOException,ServerIsClosedException {
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

    /**
     * Executa a operacao no servidor de listar viagens com escalas entre duas localizacoes
     * @param  number Numero da operacao
     * @param origem Origem da reserva
     * @param destino Destino da reserva
     * @return null em caso de erro de conexão;
     * lista vazia caso não existam voos;
     * lista com voos;
     */
    public List<List<String>> listaViagensEscalas(int number, String origem, String destino) throws ServerIsClosedException { //tag 5
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

    /**
     * Envia os dados necessarios ao servidor para executar a operacao de fazer uma reserva
     * @param  number Numero da operacao
     * @param localizacoes Rota cujas localizacoes o cliente pretende visitar
     * @param dInf Data minima para a reserva
     * @param dSup Data maxima para a reserva
     * @param tag Identificador da operacao que o servidor para ter que executar
     */
    private void sendDadosReserva(int number, List<String> localizacoes, LocalDate dInf,LocalDate dSup,int tag) throws IOException,ServerIsClosedException {
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

    /**
     *Executa a operacao no servidor de fazer uma reserva
     * @param  number Numero da operacao
     * @param localizacoes Rota cujas localizacoes o cliente pretende visitar
     * @param dInf Data minima para a reserva
     * @param dSup Data maxima para a reserva
     * @return >= 0 caso tenha sido efetuada uma reserva sendo o return value o id da reserva;
     * -1 em caso de verificacao de segurança;
     * -2 caso as localizacoes inseridas sejas invalidas;
     * -3 caso o numero de localizacoes seja invalido;
     * -4 caso o utilizador ja possua uma reserva para este dia ou nao existam lugares disponiveis
     */
    public int fazReserva(int number, List<String> localizacoes, LocalDate dInf,LocalDate dSup) throws ServerIsClosedException { //tag 6
        try {
            sendDadosReserva(number,localizacoes,dInf,dSup,6);

            try {
                Frame f = m.receive(number);
                m.finishedReceivingMessages(number);

                ByteArrayInputStream bais = new ByteArrayInputStream(f.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);

                int id = ois.readInt();

                ois.close();
                bais.close();

                if(f.getTag()==6) {
                    return id;
                } else return -5;
            } catch (Exception e) {
                return -5;
            }
        } catch (IOException e) {
            return -5;
        }
    }

    /**
     * Envia os dados necessarios ao servidor para executar a operacao de cancelar uma reserva
     * @param  number Numero da operacao
     * @param idReserva Corresponde a reserva que o utilizador queira cancelar
     * @param tag Identificador da operacao que o servidor para ter que executar
     */
    private void sendDadosCancelaReserva(int number, Integer idReserva, int tag) throws IOException,ServerIsClosedException {
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

    /**
     * Executa a operacao no servidor de cancelar uma reserva
     * @param  number Numero da operacao
     * @param idReserva Corresponde a reserva que o utilizador queira cancelar
     * @return 0 caso a reserva tenha sido removida;
     * 1 caso o id da reserva nao tenha sido encontrado nas reservas do utilizador;
     * 2 em caso de falha de segurança;
     * -2 em caso de erro de conexão
     */
    public int cancelaReserva(int number, Integer idReserva) throws ServerIsClosedException { //tag 7
        try {
            sendDadosCancelaReserva(number, idReserva, 7);
            int confirmacao = confirmacao(m.receive(number));  //0 significa que a reserva foi removida, 1 id nao existe, 2 falha de segurança
            m.finishedReceivingMessages(number);
            return confirmacao;
        } catch (IOException e) {
            return 1;
        }
    }

    /**
     * Executa a operacao no servidor de listar viagens com escalas simples (sem origem nem destino)
     * @param  number Numero da operacao
     * @return null em caso de erro de conexão;
     * lista vazia caso não existam voos;
     * lista com voos;
     */
    public List<List<String>> listaViagensEscalasSimples(int number) throws ServerIsClosedException { //tag 8
        try {
            m.send(number,8,new byte[0],false);
            Frame f = m.receive(number);
            m.finishedReceivingMessages(number);

            List<List<String>> viagens= null;
            if(f.getTag()==8) {
                viagens = Viagens.deserialize(f.getData());
            }
            return viagens;  //se for null entao houve erro de conexão
        } catch (IOException e) {
            return null;
        }
    }
}

