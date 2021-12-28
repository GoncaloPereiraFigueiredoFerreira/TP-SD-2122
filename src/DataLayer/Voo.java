package DataLayer;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Voo {

	private final String idVoo;   //Identificador do voo
	private final String origem;  //Nome da origem
	private final String destino; //Nome do destino
	private final int capacidade; //Capacidade máxima de viajantes num voo
	private final Map<LocalDate,Reservas> reservas; //Reservas relativas a este voo
	private final ReadWriteLock RWlock = new ReentrantReadWriteLock(); //Lock que permite adicionar novos dias para reservas

	/**
	 * Construtor de um voo.
	 * @param origem Nome da origem
	 * @param destino Nome do destino
	 * @param capacidade Capacidade máxima de viajantes num voo
	 */
	public Voo(String origem, String destino, int capacidade) {
		this.reservas   = new HashMap<>();
		this.idVoo 	    = origem + destino;
		this.origem     = origem;
		this.destino    = destino;
		this.capacidade = capacidade;
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

	// ****** Auxiliares ******

	/**
	 * Regista um novo dia onde este voo acontece.
	 * @param data Data na qual se pretende que haja um voo
	 * @return o objeto que permite guardar as reservas relativas a esse dia
	 */
	public Reservas registarVooNovoDia(LocalDate data){
		try {
			RWlock.writeLock().lock();
			if(reservas.get(data) == null){
				Reservas rs = new Reservas();
				reservas.put(data,rs);
				return rs;
			}
			return null;
		} finally { RWlock.writeLock().unlock(); }
	}
}