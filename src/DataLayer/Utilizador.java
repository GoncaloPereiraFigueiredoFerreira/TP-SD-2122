package DataLayer;

import java.util.*;

public class Utilizador {

	private final String idUtilizador;
	private final String password;
	private final boolean admin;
	private final Set<Integer> idsReservas = new HashSet<>();

	/**
	 * Construtor para um utilizador
	 * @param idUtilizador Identificador do utilizador
	 * @param password Palavra-passe que se pretende ter associada ao identificador
	 * @param admin Booleano que indica se deve ser ou não um administrador
	 */
	public Utilizador(String idUtilizador, String password, boolean admin) {
		this.idUtilizador = idUtilizador;
		this.password 	  = password;
		this.admin 		  = admin;
	}

	/** @return Identificador do utilizador */
	public String getIdUtilizador() { return idUtilizador; }

	/** @return Palavra-passe do utilizador */
	public String getPassword() { return password; }

	/** @return true se for um adminstrador */
 	public boolean isAdmin() { return admin; }

	/** @return coleção de todos os ids de reserva do utilizador */
	public Collection<Integer> getIdsReserva(){
 		return new ArrayList<>(idsReservas);
	}

	/** Adiciona um id de reserva */
	public void addIdReserva(Integer idReserva){
		this.idsReservas.add(idReserva);
	}

	/**
	 * Remove um id de reserva.
	 * @return true se foi possivel remover o id de reserva
	 */
	public boolean removeIdReserva(Integer idReserva){
		return this.idsReservas.remove(idReserva);
	}

	//TODO - tirar isto
	@Override
	public String toString() {
		return "Utilizador{" +
				"idUtilizador='" + idUtilizador + '\'' +
				", password='" + password + '\'' +
				", admin=" + admin +
				'}';
	}
}