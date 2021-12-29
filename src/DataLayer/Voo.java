package DataLayer;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Voo implements Comparable<Voo> {

	private String idVoo;   //Identificador do voo
	private String origem;  //Nome da origem
	private String destino; //Nome do destino
	private int capacidade; //Capacidade máxima de viajantes num voo
	private Map<LocalDate,Reservas> reservas; //Reservas relativas a este voo
	private ReadWriteLock RWlock; //Lock que permite adicionar novos dias para reservas

	/**
	 * Construtor de um voo.
	 * @param origem Nome da origem
	 * @param destino Nome do destino
	 * @param capacidade Capacidade máxima de viajantes num voo
	 */
	public Voo(String origem, String destino, int capacidade) {
		this.reservas   = new HashMap<>();
		this.idVoo 	    = origem + destino + capacidade;
		this.origem     = origem;
		this.destino    = destino;
		this.capacidade = capacidade;
		this.RWlock     = new ReentrantReadWriteLock();
	}

	// ****** Getters ******

	/** @return Identificador do voo */
	public String getIdVoo() { return idVoo; }

	/** @return Nome da localização onde o voo é iniciado */
	public String getOrigem() { return origem; }

	/** @return Nome da localização onde o voo termina */
	public String getDestino() { return destino; }

	/** @return Capacidade máxima de viajantes num voo */
	public int getCapacidade() { return capacidade; }


	// ****** Adicionar e Remover viajante ******

	/**
	 * Adiciona a reserva de um novo viajante numa dada data.
	 * @param idViajante Identificador do viajante
	 * @param data Data na qual se pretende fazer uma reserva
	 * @return booleano que indica se foi feita a reserva com sucesso.
	 */
	public boolean addViajante(String idViajante, LocalDate data) {
		Reservas rs;

		try {
			RWlock.readLock().lock();
			rs = reservas.get(data);
		}finally { RWlock.readLock().unlock(); }

		if(rs == null)
			return false;

		return rs.addViajante(idViajante,capacidade);
	}

	/**
	 * Remove a reserva de um novo viajante.
	 * @param idViajante Identificador do viajante
	 */
	public void removeViajante(String idViajante, LocalDate data) {
		Reservas rs;

		try {
			RWlock.readLock().lock();
			rs = reservas.get(data);
		}finally { RWlock.readLock().unlock(); }

		if(rs != null) rs.removeViajante(idViajante);
	}


	// ****** Lock e Unlock de reservas de um dia específico ******

	/**
	 * Usado para ganhar o lock de reservas de uma determinada data.
	 * Criado tendo em conta a atomicidade da reserva de voos para viagens com escalas.
	 * @return 'true' se conseguir obter o lock, 'false' caso contrário
	 */
	public boolean lock(LocalDate data){
		Reservas rs;

		try {
			RWlock.readLock().lock();
			rs = reservas.get(data);
		}finally { RWlock.readLock().unlock(); }

		if(rs == null && (rs = registarVooNovoDia(data)) == null)
			return false;

		rs.lock();

		return true;
	}

	/**
	 * Usado para desbloquear as reservas de uma determinada data permitindo o acesso a novas threads.
	 * Criado tendo em conta a atomicidade da reserva de voos para viagens com escalas.
	 * @return 'true' se conseguir desbloquear, 'false' caso contrário
	 */
	public boolean unlock(LocalDate data){
		Reservas rs;

		try {
			RWlock.readLock().lock();
			rs = reservas.get(data);
		}finally { RWlock.readLock().unlock(); }

		if(rs == null)
			return false;

		rs.unlock();

		return true;
	}

	// ****** Métodos auxiliares ******

	/**
	 * Regista um novo dia onde este voo acontece.
	 * @param data Data na qual se pretende que haja um voo
	 * @return o objeto que permite guardar as reservas relativas a esse dia
	 */
	public Reservas registarVooNovoDia(LocalDate data){
		try {
			RWlock.writeLock().lock();
			Reservas rs = reservas.get(data);
			if(rs == null){
				rs = new Reservas();
				reservas.put(data,rs);
			}
			return rs;
		} finally { RWlock.writeLock().unlock(); }
	}

	/**
	 * Verifica se existe possibilidade de efetuar uma reserva.
	 * De modo a obter um resultado seguro ao fazer uma reserva, sugere-se a obtenção do lock para a data inserida, antes de executar esta verificação.
	 * @param data Data na qual se pretende que haja um voo
	 * @return o objeto que permite guardar as reservas relativas a esse dia
	 */
	public boolean podeReservar (LocalDate data){
		try {
			RWlock.readLock().lock();
			Reservas rs = reservas.get(data);
			if(rs == null || rs.getNrViajantes() < capacidade)
				return true;
		} finally { RWlock.readLock().unlock(); }
		return false;
	}


	// ****** Comparacao ******

	@Override
	public int compareTo(Voo o) {
		if(o != null)
			return destino.compareTo(o.getDestino());
		return 1;
	}

	private Voo(String destino){ this.destino = destino; }

	/**
	 * Voo apenas utilizado para procuras pelo destino, que utilizem a ordem natural deste objeto.
	 * @param destino Destino que se pretende procurar
	 * @return voo com o destino pretendido.
	 * @warning Restantes variaveis do objeto não são inicializadas.
	 */
	public static Voo vooParaComparacao(String destino){
		return new Voo(destino);
	}
}