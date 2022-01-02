package DataLayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Viagem {

	private static int proxIdReserva = 0;
	private final int idReserva;
	private final String idUtilizador;
	private final Collection<String> listaVoos;
	private final LocalDate data;

	/**
	 * Construtor para uma viagem.
	 * @param idUtilizador Identificador do utilizador que reservou os voos que constituem a viagem.
	 * @param listaVoos Coleção dos identificadores dos voos que constituem a viagem
	 * @param data Data em que a viagem é suposto acontecer
	 */
	public Viagem(String idUtilizador, Collection<String> listaVoos, LocalDate data) {
		this.idReserva    = proxIdReserva++;
		this.idUtilizador = idUtilizador;
		this.listaVoos 	  = new ArrayList<>(listaVoos);
		this.data 	   	  = data;
	}

	public Viagem(Viagem viagem){
		this.idReserva 	  = viagem.idReserva;
		this.idUtilizador = viagem.idUtilizador;
		this.data		  = viagem.data;
		this.listaVoos    = new ArrayList<>(viagem.listaVoos);
	}

	public Viagem clone(){
		return new Viagem(this);
	}

	// ****** Getters ******

	/** @return Identificador do utilizador que reservou os voos que constituem a viagem */
	public String getIdUtilizador() { return idUtilizador; }

	/** @return Identificador da reserva */
	public Integer getIdReserva() { return idReserva; }

	/** @return Lista dos identificadores dos voos que constituem a viagem */
	public List<String> getColecaoVoos() { return new ArrayList<>(listaVoos); }

	/** @return Data em que a viagem é suposto acontecer */
	public LocalDate getData() { return data; }

	//tirar isto
	@Override
	public String toString() {
		return "Viagem{" +
				"idReserva=" + idReserva +
				", idUtilizador='" + idUtilizador + '\'' +
				", listaVoos=" + listaVoos +
				", data=" + data +
				'}';
	}
}