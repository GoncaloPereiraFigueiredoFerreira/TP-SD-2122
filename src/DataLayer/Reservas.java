package DataLayer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class Reservas{

	private final Set<String> viajantes; //identificadores dos utilizadores (viajantes) que fizeram reserva
	private final ReentrantLock rl; 	 //lock para a estrutura que armazena os identificadores dos viajantes

	/** Construtor para as reservas. */
	public Reservas() {
		this.viajantes = new HashSet<>();
		this.rl		   = new ReentrantLock();
	}


	// ****** Getters ******

	/** @return Set com os identificadores dos utilizadores (viajantes) que fizeram reserva */
	public Set<String> getViajantes() {
		try {
			rl.lock();
			return new HashSet<>(viajantes);
		} finally { rl.unlock(); }
	}

	/** @return Número de utilizadores (viajantes) que fizeram reserva */
	public int getNrViajantes() {
		try {
			rl.lock();
			return viajantes.size();
		} finally { rl.unlock(); }
	}

	// ****** Adicionar e Remover viajante ******

	/**
	 * Adiciona a reserva de um novo viajante.
	 * @param idViajante Identificador do viajante
	 * @param nrMaxViajantes Capacidade máxima de viajantes
	 * @return boolean indicando se foi possível efetuar a reserva.
	 */
	public boolean addViajante(String idViajante, int nrMaxViajantes) {
		try{
			rl.lock();
			if(viajantes.size() < nrMaxViajantes) {
				viajantes.add(idViajante);
				return true;
			}
		}
		finally { rl.unlock(); }
		return false;
	}

	/**
	 * Remove a reserva de um novo viajante.
	 * @param idViajante Identificador do viajante
	 */
	public void removeViajante(String idViajante) {
		try{
			rl.lock();
			viajantes.remove(idViajante); }
		finally { rl.unlock(); }
	}

	/**
	 * Verifica a existencia de um viajante
	 * @param idViajante Identificador do viajante
	 */
	public boolean verificaExistenciaViajante(String idViajante) {
		try{
			rl.lock();
			return viajantes.contains(idViajante); }
		finally { rl.unlock(); }
	}

	// ****** Lock e Unlock das reservas ******

	/**
	 * Usado para ganhar o lock das reservas.
	 * Criado tendo em conta a atomicidade da reserva de voos para viagens com escalas.
	 */
	public void lock(){ rl.lock(); }

	/** Usado para desbloquear as reservas permitindo o acesso a novas threads. */
	public void unlock(){ rl.unlock(); }

	//TODO - tirar isto
	@Override
	public String toString() {
		return "Reservas{" +
				"viajantes=" + viajantes +
				'}';
	}
}