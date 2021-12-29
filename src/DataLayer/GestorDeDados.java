package DataLayer;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class GestorDeDados {

	//Utilizadores
	private final Map<String,Utilizador> utilizadores = new HashMap<>();
	private final ReentrantLock usersLock 			  = new ReentrantLock();

	//Voos
	private final Map<String,Voo> voos     			  = new HashMap<>();
	private final ReadWriteLock voosRwLock			  = new ReentrantReadWriteLock();
	private final Map<String,TreeSet<Voo>> grafoVoos  = new HashMap<>();

	//Viagens
	private final Map<Integer,Viagem> viagens = new HashMap<>();
	private final ReentrantLock viagensLock   = new ReentrantLock();

	//Dias Encerrados
	private final Set<LocalDate> diasEncerrados    = new HashSet<>();
	private final ReentrantLock diasEncerradosLock = new ReentrantLock();

	//TODO - remover estes prints

	public void printUtilizadores(){
		System.out.println("******* UTILIZADORES *******\n");
		for(Utilizador u : utilizadores.values())
			System.out.println(u);
	}

	public void printVoos(){
		System.out.println("******* VOOS *******\n");
		for(Map.Entry<String, Voo> e : voos.entrySet())
			System.out.println("id: " + e.getKey() + " | voo: " + e.getValue());
	}

	public void printviagens(){
		System.out.println("******* VIAGENS *******\n");
		for(Map.Entry<Integer, Viagem> e : viagens.entrySet())
			System.out.println("id: " + e.getKey() + " | viagem: " + e.getValue());
	}

	public void printDiasEncerrados(){
		System.out.println(diasEncerrados);
	}

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

		//Verifica existência do voo
		if (encontraVoo(origem, destino) != null) return false;

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

	//TODO - devo partir em bocados para evitar tanto tempo com o readLock, ou será que devo manter a atomicidade da operacao e abranger a procura dos voos no lock?
	public Integer fazRevervasViagem(String idUtilizador, List<String> localizacoes, LocalDate dataInicial, LocalDate dataFinal) {
		List<Voo> voosOrdenados;
		LocalDate data;
		LocalDate dataReserva = null;

		//Não pode executar reservas sem ter pelo menos uma origem e um destino
		if (localizacoes.size() <= 1) return null;


		//Encontra voos e ordena-os, de forma a impedir deadLocks
		voosOrdenados = new ArrayList<>();
		for (int i = 0; i < localizacoes.size() - 1; i++) {
			Voo voo = encontraVoo(localizacoes.get(i), localizacoes.get(i + 1));

			//Se as localizações fornecidas forem inválidas não pode fazer uma reserva
			if (voo == null) return null;

			voosOrdenados.add(voo);
		}
		voosOrdenados.sort(null);


		try {
			voosRwLock.readLock().lock();

			//Tenta efetuar as reservas
			boolean podeReservar;

			for (data = dataInicial; dataReserva == null && (data.isBefore(dataFinal) || data.isEqual(dataFinal)); data = data.plusDays(1)) {
				podeReservar = true;

				if (!isDayClosed(data)) {
					try {
						//Obtem os locks de todas as reservas que pretende fazer
						for (Voo voo : voosOrdenados) {
							voo.lock(data);
						}

						//Verifica disponibilidade para reserva
						for (int i = 0; podeReservar && i < voosOrdenados.size(); i++) {

							Voo voo = voosOrdenados.get(i);

							if (!voo.podeReservar(data))
								podeReservar = false;
						}

						//Faz as reservas se houver disponibilidade
						if (podeReservar) {

							for (Voo voo : voosOrdenados)
								voo.addViajante(idUtilizador, data);

							dataReserva = data;
						}
					} finally {
						for (Voo voo : voosOrdenados)
							voo.unlock(data);
					}
				}
			}
		} finally { voosRwLock.readLock().unlock(); }

		if (dataReserva == null) return null;

		return addViagem(idUtilizador, voosOrdenados.stream().map(Voo::getIdVoo).collect(Collectors.toList()), dataReserva);

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
	 * Remove a viagem e as reservas feitas em todos os voos que constituem a viagem.
	 * @param idReserva Identificador da reserva da viagem
	 * @return true se foi possível remover com sucesso todas as reservas;
	 * false caso não exista uma viagem com o identificador de reserva fornecido
	 */
	public boolean removeReservasEViagem(String idUtilizador, int idReserva){
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
	 * @param origem Nome da origem
	 * @param destino Nome da destino
	 * @return o voo se o encontrar, ou 'null' se não encontrar o voo.
	 */
	private Voo encontraVoo(String origem, String destino) {
		try {
			voosRwLock.readLock().lock();

			TreeSet<Voo> voosDaOrigem = grafoVoos.get(origem);

			if (voosDaOrigem != null) {
				//TODO - tentar mudar isto. É feio mas é mais rápido do que procurar um a um numa lista
				Voo voo = voosDaOrigem.floor(Voo.vooParaComparacao(destino));
				if (voo != null && voo.getDestino().equals(destino))
					return voo;
			}

			return null;

		} finally { voosRwLock.readLock().unlock(); }
	}

	/*private Voo encontraVoo(String origem, String destino){
		List<Voo> voosDaOrigem = grafoVoos.get(origem);

		for (Voo voo : voosDaOrigem)
			if(voo.getDestino().equals(destino))
				return voo;

		return null;
	}*/
}