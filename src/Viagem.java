import java.time.LocalDate;
import java.util.List;

public class Viagem {

	private String idReserva;
	private List<String> listaVoos;
	private LocalDate data;

	public Viagem(String idReserva, List<String> listaVoos, LocalDate data) {
		this.idReserva = idReserva;
		this.listaVoos = listaVoos;
		this.data 	   = data;
	}
}