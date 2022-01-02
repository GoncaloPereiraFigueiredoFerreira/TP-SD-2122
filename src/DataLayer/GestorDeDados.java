package DataLayer;

import DataLayer.Exceptions.*;

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
	private final Map<String,Voo> voos     			      = new HashMap<>();
	private final ReadWriteLock voosRwLock			      = new ReentrantReadWriteLock();
	private final Map<String, Map<String,Voo>> grafoVoos  = new HashMap<>();

	//Viagens
	private final Map<Integer,Viagem> viagens 		  = new HashMap<>();
	private final ReentrantLock viagensLock  		  = new ReentrantLock();

	//Dias Encerrados
	private final Set<LocalDate> diasEncerrados    	  = new HashSet<>();
	private final ReentrantLock diasEncerradosLock 	  = new ReentrantLock();

	private static final int MAXVOOS = 3;

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

	// ---------- Métodos Gerais ---------- //

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


	/**
	 * Regista um novo voo.
	 * @param origem Nome da origem
	 * @param destino Nome do destino
	 * @param capacidade Capacidade máxima de viajantes no voo
	 * @return true se foi registado com sucesso.
	 */
	public boolean addVoo(String origem, String destino, int capacidade){
		try {
			voosRwLock.writeLock().lock();

			//Verifica existência do voo
			if (encontraVoo(origem, destino) != null) return false;


			//Cria o voo a inserir
			Voo voo = new Voo(origem, destino, capacidade);
			String idVoo = voo.getIdVoo();

			//Se ainda não existir o voo, regista-o e acrescenta uma entrada
			//na lista de voos começados pela localização de origem fornecida
			if (!voos.containsKey(idVoo)) {

				voos.put(idVoo, voo);

				Map<String,Voo> l = grafoVoos.computeIfAbsent(origem, k -> new HashMap<>());

				l.put(destino,voo);

				return true;
			}

		} finally { voosRwLock.writeLock().unlock(); }

		return false;
	}


	/**
	 * Reserva os voos que passam pelas localizações fornecidas, no intervalo fornecido.
	 * @param idUtilizador Identificador do utilizador que pretende fazer a reserva
	 * @param localizacoes Localizações, organizadas pela ordem dos voos, que constituem a viagem
	 * @param dataInicial Limite inferior do intervalo na qual se pretende a reserva
	 * @param dataFinal Limite superior do intervalo na qual se pretende a reserva
	 * @return o identificador da reserva caso tenha sido bem sucedida. 'null' caso não seja possível efetuar a reserva devido a falta de capacidade nos voos.
	 * @throws numeroLocalizacoesInvalidoException Se o número de localizações fornecidas for menor ou igual a 1, ou se for superior a (MAXVOOS + 1).
	 * @throws localizacoesInvalidasException Se não existir alguma das localizações fornecidas.
	 */
	public InformacaoSobreReserva fazRevervasViagem(String idUtilizador, List<String> localizacoes, LocalDate dataInicial, LocalDate dataFinal) throws numeroLocalizacoesInvalidoException, localizacoesInvalidasException {
		List<Voo> voosOrdenados;
		LocalDate data;
		LocalDate dataReserva = null;

		//Não pode executar reservas sem ter pelo menos uma origem e um destino
		if (localizacoes.size() <= 1 || localizacoes.size() > MAXVOOS + 1)
			throw new numeroLocalizacoesInvalidoException();

		voosOrdenados = new ArrayList<>();

		try {
			voosRwLock.readLock().lock();

			//Encontra voos e ordena-os, de forma a impedir deadLocks
			for (int i = 0; i < localizacoes.size() - 1; i++) {
				Voo voo = encontraVoo(localizacoes.get(i), localizacoes.get(i + 1));

				//Se as localizações fornecidas forem inválidas não pode fazer uma reserva
				if (voo == null)
					throw new localizacoesInvalidasException();

				voosOrdenados.add(voo);
			}
			voosOrdenados.sort(null);

			//Tenta efetuar as reservas
			boolean podeReservar;

			for (data = dataInicial; dataReserva == null && (data.isBefore(dataFinal) || data.isEqual(dataFinal)); data = data.plusDays(1)) {
				podeReservar = true;

				if (!isDayClosed(data)) {
					try {
						//Obtem os locks de todas as reservas que pretende fazer
						for (Voo voo : voosOrdenados)
							voo.lock(data);

						//Verifica disponibilidade para reserva
						for (int i = 0; podeReservar && i < voosOrdenados.size(); i++) {
							Voo voo 	 = voosOrdenados.get(i);
							podeReservar = voo.podeReservar(idUtilizador, data);
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

			if (dataReserva == null) return null;

			viagensLock.lock();
			usersLock.lock();
		} finally { voosRwLock.readLock().unlock(); }

		Integer idReserva;
		try {
			//Regista a reserva da viagem
			try { idReserva = addViagem(idUtilizador, voosOrdenados.stream().map(Voo::getIdVoo).collect(Collectors.toList()), dataReserva); }
			finally { viagensLock.unlock(); }

			//Adiciona o id da reserva à colecao do utilizador
			utilizadores.get(idUtilizador).addIdReserva(idReserva);
		} finally { usersLock.unlock(); }

		return new InformacaoSobreReserva(idReserva, dataReserva, localizacoes);
	}


	/**
	 * Remove a viagem e as reservas feitas em todos os voos que constituem a viagem.
	 * @param idReserva Identificador da reserva da viagem
	 * @return true se foi possível remover com sucesso todas as reservas;
	 * false caso não exista uma viagem com o identificador de reserva fornecido
	 */
	public boolean removeReservasEViagem(String idUtilizador, int idReserva){
		Viagem viagem;

		try {
			usersLock.lock();

			//Verifica existencia do utilizador e se o identificador da reserva pertence à colecao deste
			Utilizador u = utilizadores.get(idUtilizador);
			if(u == null || !u.removeIdReserva(idReserva)) return false;

			try {
				viagensLock.lock();

				//Remove a reserva
				viagem = viagens.get(idReserva);

				if(viagem == null || !viagem.getIdUtilizador().equals(idUtilizador) || isDayClosed(viagem.getData())) return false;

				viagens.remove(idReserva);

				voosRwLock.readLock().lock();

			} finally { viagensLock.unlock(); }

		}finally { usersLock.unlock(); }

		try {
			for(String idVoo : viagem.getColecaoVoos())
				removeReserva(idVoo, idUtilizador, viagem.getData());
		} finally { voosRwLock.readLock().unlock(); }

		return true;
	}

	/**
	 * @param idUtilizador Identificador do utilizador do qual se pretende verificar a lista de viagens
	 * @return null se o utilizador não existir, ou uma colecao com todas as reservas do utilizador.
	 */
	public Collection<InformacaoSobreReserva> listaViagensUtilizador(String idUtilizador){
		Collection<Integer> idsReservas;
		Collection<Viagem> viagensUser;

		try {
			//Verifica existencia do utilizador e recolhe os ids de todas as reservas feitas por este
			usersLock.lock();
			Utilizador u = utilizadores.get(idUtilizador);
			if(u == null) return null;
			idsReservas = u.getIdsReserva();

			//Encontra todas as viagens através do respetivo id
			try {
				viagensLock.lock();
				viagensUser = idsReservas.stream().map(viagens::get).collect(Collectors.toList()); }
			finally { viagensLock.unlock(); }

			voosRwLock.readLock().lock();
		} finally { usersLock.unlock(); }

		//Encontra as localizacoes pertencentes a cada uma das viagens e gera uma lista com as informacoes de cada reserva
		try{
			Collection<InformacaoSobreReserva> listaInformacoesReservas = new ArrayList<>();
			for(Viagem v : viagensUser){
				List<String> idsVoos = v.getColecaoVoos(),
							 locais  = new ArrayList<>();

				//Adiciona a origem e o destino do primeiro voo
				Voo voo = voos.get(idsVoos.get(0));
				locais.add(voo.getOrigem());
				locais.add(voo.getDestino());

				//Se houverem mais voos, basta ir buscar o destino de cada um
				for(int i = 1; i < idsVoos.size() ; i++)
					locais.add(voos.get(idsVoos.get(i)).getDestino());

				listaInformacoesReservas.add(new InformacaoSobreReserva(v.getIdReserva(), v.getData(), locais));
			}

			return listaInformacoesReservas;
		} finally { voosRwLock.readLock().unlock(); }
	}

	/**
	 * Procura e devolve todos os voos possíveis.
	 * @return lista com todos os voos possíveis.
	 */
	public List<List<String>> listaVoosExistentes(){
		try {
			voosRwLock.readLock().lock();

			List<List<String>> listaVoosExistentes = new ArrayList<>();
			Set<String> setOrigens = grafoVoos.keySet(); //Set das origens dos voos

			for (String origem : setOrigens){
				Map<String,Voo> voos = grafoVoos.get(origem);

				if(voos == null) return null;

				voos.keySet().forEach(destino -> listaVoosExistentes.add(new ArrayList<>(Arrays.asList(origem, destino))));
			}

			return listaVoosExistentes;

		}finally { voosRwLock.readLock().unlock(); }
	}

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
	// ---------- Métodos Auxiliares Sem Locks ---------- //

	/**
	 * Remove a reserva de um determinado utilizador para um certo voo.
	 * @param idVoo Identificador do voo no qual se pretende remover a reserva
	 * @param idUtilizador Identificador do utilizador que pretende remover a reserva
	 * @param data Data do voo em que se pretende cancelar a reserva
	 * @warning Necessita do readLock dos voos.
	 */
	private void removeReserva(String idVoo, String idUtilizador, LocalDate data) {
		Voo voo = voos.get(idVoo);
		voo.removeViajante(idUtilizador, data);
	}

	/**
	 * Adiciona uma reserva de viagem.
	 * @param idUtilizador Identificador do utilizador que fez a reserva
	 * @param voos Coleção dos identificadores dos voos que constituem a viagem
	 * @param data Data na qual é suposto acontecer a viagem
	 * @return o identificador da reserva da viagem
	 * @warning Necessita do lock das viagens
	 */
	public Integer addViagem(String idUtilizador, List<String> voos, LocalDate data) {
		Viagem viagem = new Viagem(idUtilizador, voos, data);

		Integer idReserva = viagem.getIdReserva();

		viagens.put(idReserva, viagem);

		return idReserva;
	}

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
		Map<String,Voo> map_voos = grafoVoos.get(nodoAtual); //Map dos voos que partem do nodo atual

		if (map_voos != null && !caminhoAtual.contains(nodoAtual)) {
			caminhoAtual = new ArrayList<>(caminhoAtual);
			caminhoAtual.add(nodoAtual);

			for (String destino : map_voos.keySet()) {

				//Adiciona o caminho até o nodo Atual caso encontre o destino
				if (destino.equals(nodoFinal)) {
					List<String> novoCaminho = new ArrayList<>(caminhoAtual);
					novoCaminho.add(nodoFinal);
					listaDeCaminhos.add(novoCaminho);
				}
				//Se ainda não tiver encontrado o nodo final e não tiver atingido a profundidade máxima continua a procurar
				else if (depthAtual < depthMax)
					pesquisaTodosEmProfundidade(destino, nodoFinal, depthAtual + 1, depthMax, listaDeCaminhos, caminhoAtual);
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
		Map<String,Voo> map_voos = grafoVoos.get(nodoAtual);

		if(!caminhoAtual.contains(nodoAtual)) {
			caminhoAtual = new ArrayList<>(caminhoAtual);
			caminhoAtual.add(nodoAtual);

			if(caminhoAtual.size() > 1)
				listaDeCaminhos.add(new ArrayList<>(caminhoAtual));

			if (map_voos != null) {
				for (String destino : map_voos.keySet())
					if (depthAtual <= depthMax)
						travessiaEmProfundidade(destino, depthAtual + 1, depthMax, listaDeCaminhos, caminhoAtual);
			}
		}
	}

	/**
	 * Encontra o voo com a origem e destino fornecidos.
	 * @param origem Nome da origem
	 * @param destino Nome da destino
	 * @return o voo se o encontrar, ou 'null' se não encontrar o voo.
	 * @warning Necessita do readLock/writeLock dos voos
	 */
	private Voo encontraVoo(String origem, String destino) {
		Map<String,Voo> voosDaOrigem = grafoVoos.get(origem);

		if (voosDaOrigem != null)
			return voosDaOrigem.get(destino);

		return null;
	}

}
