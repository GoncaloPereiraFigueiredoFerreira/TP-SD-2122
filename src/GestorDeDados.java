import java.time.LocalDate;
import java.util.*;

public class GestorDeDados {

	private final Map<String, Utilizador> utilizadores = new HashMap<>();
	private final Map<String, Voo> voos 			   = new HashMap<>();
	private final Map<String, Integer> viagens 		   = new HashMap<>();
	private final Map<String, List<String>> grafoVoos  = new HashMap<>();
	private final Set<LocalDate> diasEncerrados 	   = new HashSet<>();;


}