package DataLayer;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GestorDeDados {

	//Utilizadores
	private final Map<String,Utilizador> utilizadores = new HashMap<>();
	private final ReentrantLock usersLock = new ReentrantLock();

	//Voos
	private final Map<String,Voo> voos     = new HashMap<>();
	private final ReadWriteLock voosRwLock = new ReentrantReadWriteLock();
	private final Map<String,Set<Voo>> grafoVoos  = new HashMap<>();

	//Viagens
	private final Map<Integer,Viagem> viagens = new HashMap<>();
	private final ReentrantLock viagensLock = new ReentrantLock();

	//Dias Encerrados
	private final Set<LocalDate> diasEncerrados    = new HashSet<>();
	private final ReentrantLock diasEncerradosLock = new ReentrantLock();

	// ****** Métodos Gerais ******

	/**
	 * Regista utilizador/administrador.
	 * @param idUtilizador Identificador do utilizador
	 * @param password Palavra-passe que o utilizador pretende usar para se autenticar
	 * @param admin Booleano que indica se a conta a criar é administradora ou não
	 * @return booleano que indica se a operação foi um sucesso
	 */
	public boolean addUtilizador(String idUtilizador, String password, boolean admin){
		try {
			usersLock.lock();
			if (utilizadores.containsKey(idUtilizador)) return false;
			utilizadores.put(idUtilizador, new Utilizador(idUtilizador, password, admin));
			return true;
		} finally { usersLock.unlock(); }
	}

	/**
	 * Verifica se as credenciais fornecidas pertencem a algum utilizador.
	 * @param idUtilizador Identificador do utilizador utilizado para autenticar
	 * @param password Palavra-passe utilizada para autenticar
	 * @return 0 se as credenciais estiverem corretas e pertencerem a um utilizador padrão;
	 * 		   1 se as credenciais estiverem corretas e pertencerem a um administrador;
	 *        -1 se não existir um utilizador com as credenciais fornecidas
	 */
	public int verificaCredenciais(String idUtilizador, String password){
		try {
			usersLock.lock();

			Utilizador u = utilizadores.get(idUtilizador);
			if(u != null && u.getPassword().equals(password)) {
				if(u.isAdmin()) return 1;
				else return 0;
			}

			return -1;

		} finally { usersLock.unlock(); }
	}


	// ****** Métodos dos Voos ******

	/**
	 * Regista um novo voo.
	 * @param origem Nome da origem
	 * @param destino Nome do destino
	 * @param capacidade Capacidade máxima de viajantes no voo
	 * @return true se foi registado com sucesso.
	 */
	public boolean addVoo(String origem, String destino, int capacidade){
		//Cria o voo a inserir
		Voo voo = new Voo(origem, destino, capacidade);
		String idVoo = voo.getIdVoo();

		try {
			voosRwLock.writeLock().lock();

			//Se ainda não existir o voo, regista-o e acrescenta uma entrada
			//na lista de voos começados pela localização de origem fornecida
			if(!voos.containsKey(idVoo)) {

				voos.put(idVoo, voo);

				Set<Voo> l = grafoVoos.computeIfAbsent(origem, k -> new TreeSet<>());

				l.add(voo);

				return true;
			}

		} finally { voosRwLock.writeLock().unlock(); }

		return false;
	}

	//Faz reservas ao contrario da anterior
	public Integer fazRevervasViagem(String idUtilizador, List<String> localizacoes, LocalDate dataInicial, LocalDate dataFinal){


		//Ordena voos, de forma a impedir deadLocks
		/*List<String> voosOrdenados = new ArrayList<>(voos);
		voosOrdenados.sort(null);

		boolean reservado = false;
		for(LocalDate data = dataInicial; reservado == false && data.isBefore(dataFinal); data = data.plusDays(1)){
			//Obtem os locks de todas as reservas que pretende fazer
			for()
		}*/
		//TODO
		throw new UnsupportedOperationException();
	}

	private void addReserva(String idVoo, String idUtilizador, LocalDate data) {
		try {
			voosRwLock.readLock().lock();

			Voo voo = voos.get(idVoo);

			voo.addViajante(idUtilizador, data);

		} finally { voosRwLock.readLock().unlock(); }
	}

	private void removeReserva(String idVoo, String idUtilizador, LocalDate data) {
		try {
			voosRwLock.readLock().lock();

			Voo voo = voos.get(idVoo);

			voo.removeViajante(idUtilizador, data);

		} finally { voosRwLock.readLock().unlock(); }
	}

	// ****** Métodos dos Grafos dos Voos ******

	/**
	 * Procura e devolve todas as viagens possíveis, com um máximo de duas escalas.
	 * @return lista com todas as viagens possíveis a partir de uma origem até um destino, com um máximo de duas escalas.
	 */
	public List<List<String>> listaViagensExistentes(){
		try {
			voosRwLock.readLock().lock();

			List<List<String>> listaViagensExistentes = new ArrayList<>();
			Set<String> setOrigens = grafoVoos.keySet(); //Set das origens dos voos

			for (String origem : setOrigens)
				travessiaEmProfundidade(origem, 1, 3, listaViagensExistentes, new ArrayList<>());

			return listaViagensExistentes;

		}finally { voosRwLock.readLock().unlock(); }
	}

	/**
	 * Procura e devolve todas as viagens possíveis a partir de uma origem até um destino, com um máximo de duas escalas.
	 * @param origem Nome da origem
	 * @param destino Nome do destino
	 * @return lista com todas as viagens possíveis a partir de uma origem até um destino, com um máximo de duas escalas.
	 */
	public List<List<String>> listaViagensExistentes(String origem, String destino){
		try {
			voosRwLock.readLock().lock();

			List<List<String>> listaViagensExistentes = new ArrayList<>();

			//Profundidade Máxima de3 graus (Grau 3 para permitir encontrar destinos fazendo um máximo de 2 escalas).
			pesquisaTodosEmProfundidade(origem, destino, 1, 3, listaViagensExistentes, new ArrayList<>());

			return listaViagensExistentes;
		}finally { voosRwLock.readLock().unlock(); }
	}

	// ****** Métodos das Viagens ******

	/**
	 * Adiciona uma reserva de viagem.
	 * @param idUtilizador Identificador do utilizador que fez a reserva
	 * @param voos Coleção dos identificadores dos voos que constituem a viagem
	 * @param data Data na qual é suposto acontecer a viagem
	 * @return o identificador da reserva da viagem
	 */
	public Integer addViagem(String idUtilizador, List<String> voos, LocalDate data){
		try {
			viagensLock.lock();

			Viagem viagem = new Viagem(idUtilizador, voos, data);

			Integer idReserva = viagem.getIdReserva();

			viagens.put(idReserva, viagem);

			return idReserva;
		}finally { viagensLock.unlock(); }
	}

	/**
	 * Remove as reservas feitas em todos os voos que constituem a viagem.
	 * @param idReserva Identificador da reserva da viagem
	 * @return true se foi possível remover com sucesso todas as reservas;
	 * false caso não exista uma viagem com o identificador de reserva fornecido
	 */
	public boolean removeViagem(String idUtilizador, int idReserva){
		try {
			viagensLock.lock();

			Viagem viagem = viagens.get(idReserva);

			if(viagem == null || !viagem.getIdUtilizador().equals(idUtilizador)) return false;

			viagens.remove(idReserva);

			for(String idVoo : viagem.getColecaoVoos())
				removeReserva(idVoo, idUtilizador, viagem.getData());

		}finally { viagensLock.unlock(); }

		return true;
	}

	// ****** Métodos dos Dias Encerrados ******

	/**
	 * Fecha um dia, não permitindo mais reservas de voos para esse dia.
	 * @param date Data que se pretende fechar
	 */
	public void closeDay(LocalDate date){
		try{
			diasEncerradosLock.lock();
			diasEncerrados.add(date);
		} finally { diasEncerradosLock.unlock(); }
	}

	/**
	 * Verifica se um dia está fechado.
	 * @param date Data que se pretende verificar se está fechado
	 * @return true se estiver fechado
	 */
	public boolean isDayClosed(LocalDate date){
		try{
			diasEncerradosLock.lock();
			return diasEncerrados.contains(date);
		} finally { diasEncerradosLock.unlock(); }
	}


	// ****** Auxiliares ******

	/**
	 * Efetua pesquisa em profundidade, de todos os caminhos que chegam ao nodo final fornececido. A profundidade é limitada.
	 * Admite que já se possui o readLock para ver o grafo e o map de voos.
	 * @param nodoAtual Nome da localização onde se pretende iniciar a pesquisa em profundidade
	 * @param nodoFinal Nome da localização onde se pretende acabar a pesquisa em profundidade
	 * @param depthAtual Profundidade dos nodos a verificar nesta iteração. Para inicializar a pesquisa o valor deve ser 1.
	 * @param depthMax Profundidade Máxima, na qual deixará de procurar.
	 * @param listaDeCaminhos Lista onde se pretende que sejam inseridos os caminhos
	 * @param caminhoAtual Lista dos nodos que já foram visitados
	 * @warning Necessita obtenção do ReadLock dos voos por parte da função chamadora.
	 */
	private void pesquisaTodosEmProfundidade(String nodoAtual, String nodoFinal, int depthAtual, int depthMax, List<List<String>> listaDeCaminhos, List<String> caminhoAtual) {
		Set<Voo> l_voos = grafoVoos.get(nodoAtual); //Lista dos ids dos voos que partem do nodo inicial

		if (l_voos != null && !caminhoAtual.contains(nodoAtual)) {
			caminhoAtual = new ArrayList<>(caminhoAtual);
			caminhoAtual.add(nodoAtual);

			for (Voo voo : l_voos) {
				String destinoVoo = voo.getDestino();

				//Adiciona o caminho até o nodo Atual caso encontre o destino
				if (destinoVoo.equals(nodoFinal)) {
					List<String> novoCaminho = new ArrayList<>(caminhoAtual);
					novoCaminho.add(nodoFinal);
					listaDeCaminhos.add(novoCaminho);
				}
				//Se ainda não tiver encontrado o nodo final e não tiver atingido a profundidade máxima continua a procurar
				else if (depthAtual < depthMax)
					pesquisaTodosEmProfundidade(destinoVoo, nodoFinal, depthAtual + 1, depthMax, listaDeCaminhos, caminhoAtual);
			}
		}
	}

	/**
	 * Efetua pesquisa em profundidade, de todos os caminhos que comecam no nodo fornececido. A profundidade é limitada.
	 * Admite que já se possui o readLock para ver o grafo e o map de voos.
	 * @param nodoAtual Nodo atual
	 * @param depthAtual Profundidade do nodo atual
	 * @param depthMax Profundidade máxima, na qual deixará de procurar.
	 * @param listaDeCaminhos Lista onde se armazena os caminhos encontrados
	 * @param caminhoAtual Caminho atual
	 * @warning Necessita obtenção do ReadLock dos voos por parte da função chamadora.
	 */
	private void travessiaEmProfundidade(String nodoAtual, int depthAtual, int depthMax, List<List<String>> listaDeCaminhos, List<String> caminhoAtual) {
		Set<Voo> l_voos = grafoVoos.get(nodoAtual);

		if(!caminhoAtual.contains(nodoAtual)) {
			caminhoAtual = new ArrayList<>(caminhoAtual);
			caminhoAtual.add(nodoAtual);

			if(caminhoAtual.size() > 1)
				listaDeCaminhos.add(new ArrayList<>(caminhoAtual));

			if (l_voos != null) {
				for (Voo voo : l_voos) {
					String destinoVoo = voo.getDestino();
					if (depthAtual <= depthMax)
						travessiaEmProfundidade(destinoVoo, depthAtual + 1, depthMax, listaDeCaminhos, caminhoAtual);
				}
			}
		}
	}

	/**
	 * Encontra o voo com a origem e destino fornecidos.
	 * @param origem
	 * @param destino
	 * @return o voo se o encontrar, ou 'null' se não encontrar o voo.
	 * @warning Necessita obtenção do ReadLock dos voos por parte da função chamadora.
	 */
	private Voo encontraVoo(String origem, String destino){
		Set<Voo> voosDaOrigem = grafoVoos.get(origem);

		if(voosDaOrigem != null) {

			Iterator<Voo> it = voosDaOrigem.iterator();

			/*while (it.hasNext()){
				it = it.next();
			}
			Voo voo;

			for ()*/
		}

		return null;
	}
}