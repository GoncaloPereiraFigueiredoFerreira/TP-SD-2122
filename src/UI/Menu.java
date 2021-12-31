package UI;

import java.util.*;

/**
 * Esta classe implementa um Menu em modo texto.
 *
 * @author José Creissac Campos
 * @version v3.2 (20201215)
 */
public class Menu {
    String nome;
    private int opcao; //Guarda a última opcao usada

    // Interfaces auxiliares

    /** Functional interface para handlers. */
    public interface Handler {  // método de tratamento
        public void execute();
    }

    /** Functional interface para pre-condicoes. */
    public interface PreCondition {  // Predicate ?
        public boolean validate();
    }

    // Varíável de classe para suportar leitura

    private static Scanner is = new Scanner(System.in);

    // Variáveis de instância

    private List<String> opcoes;            // Lista de opções
    private List<PreCondition> disponivel;  // Lista de pré-condições
    private List<Handler> handlers;         // Lista de handlers
    private Handler handlerSaida;

    // Construtor

    /**
     * Constructor for objects of class Menu
     * @param opcoes Lista de opcoes do menu
     */
    public Menu(String[] opcoes) {
        this.nome = "Menu";
        this.opcoes = Arrays.asList(opcoes);
        this.disponivel = new ArrayList<>();
        this.handlers = new ArrayList<>();
        this.opcoes.forEach(s-> {
            this.disponivel.add(()->true);
            this.handlers.add(()->System.out.println("\nATENÇÃO: Opcao não implementada!"));
        });
        this.handlerSaida = null;
    }

    /**
     * Constructor for objects of class Menu
     * @param nome Nome do Menu
     * @param opcoes Lista de opcoes do menu
     */
    public Menu(String nome, String[] opcoes) {
        this.nome = "Menu";
        this.opcoes = Arrays.asList(opcoes);
        this.disponivel = new ArrayList<>();
        this.handlers = new ArrayList<>();
        this.opcoes.forEach(s-> {
            this.disponivel.add(()->true);
            this.handlers.add(()->System.out.println("\nATENÇÃO: Opcao não implementada!"));
        });
    }

    // Métodos de instância

    /**
     * Correr o NewMenu.
     *
     * Termina com a opcao 0 (zero).
     */
    public void run() {
        int op;
        do {
            show();
            op = readOption();
            // testar pré-condição
            if (op>0 && !this.disponivel.get(op-1).validate()) {
                System.out.println("Opção indisponível! Tente novamente.");
            } else if (op>0) {
                // executar handler
                this.handlers.get(op-1).execute();
            }
        } while (op != 0);

        if(handlerSaida != null)
            handlerSaida.execute();
    }

    /**
     * Corre Menu enquanto nao for escolhida uma opcao valida.
     */
    public void runOneTime() {
        do {
            show();
            this.opcao = readOption();
            // testar pré-condição
            if (this.opcao>0 && !this.disponivel.get(this.opcao-1).validate()) {
                System.out.println("Opção indisponível! Tente novamente.");
            } else if (this.opcao>0) {
                // executar handler
                this.handlers.get(this.opcao-1).execute();
            }
        } while (this.opcao == -1);

        if(this.opcao == 0 && handlerSaida != null)
            handlerSaida.execute();
    }

    /**
     * @return ultima opcao do utilizador
     */
    public int getLastOption() {
        return opcao;
    }

    /**
     * Metodo que regista uma uma pre-condicao numa opcao do NewMenu.
     *
     * @param i indice da opcao (começa em 1)
     * @param b pre-condicao a registar
     */
    public void setPreCondition(int i, PreCondition b) {
        this.disponivel.set(i-1,b);
    }

    /**
     * Método para registar um handler numa opcao do NewMenu.
     *
     * @param i indice da opcao  (começa em 1)
     * @param h handlers a registar
     */
    public void setHandler(int i, Handler h) {
        this.handlers.set(i-1, h);
    }

    public void setHandlerSaida(Handler h) {
        this.handlerSaida = h;
    }

    // Métodos auxiliares

    /** Apresentar o Menu */
    private void show() {
        System.out.println("\n *** " + this.nome + " *** ");
        for (int i=0; i<this.opcoes.size(); i++) {
            System.out.print(i+1);
            System.out.print(" - ");
            System.out.println(this.disponivel.get(i).validate()?this.opcoes.get(i):"---");
        }
        System.out.println("0 - Sair");
    }

    /** Ler uma opcao valida */
    private int readOption() {
        int op;
        //Scanner is = new Scanner(System.in);

        System.out.print("Opção: ");
        try {
            String line = is.nextLine();
            op = Integer.parseInt(line);
        }
        catch (NumberFormatException e) { // Não foi inscrito um int
            op = -1;
        }
        if (op<0 || op>this.opcoes.size()) {
            System.out.println("Opção Inválida!!!");
            op = -1;
        }
        return op;
    }
}

