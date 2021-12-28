public class Utilizador {

	private final String idUtilizador;
	private final String password;
	private final boolean admin;

	public Utilizador(String idUtilizador, String password, boolean admin) {
		this.idUtilizador = idUtilizador;
		this.password 	  = password;
		this.admin 		  = admin;
	}

	public String getIdUtilizador() { return idUtilizador; }

	public String getPassword() { return password; }

 	public boolean isAdmin() { return admin; }
}