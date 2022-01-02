package DataLayer;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InformacaoSobreReserva implements Serializable {
    private int idReserva;
    private LocalDate dataReserva;
    private List<String> localizacoes;

    /**
     * @param idReserva Identificador da reserva
     * @param dataReserva Data em que os voos foram reservados
     * @param localizacoes Localizacoes que constituem a viagem reservada
     */
    public InformacaoSobreReserva(int idReserva, LocalDate dataReserva, Collection<String> localizacoes){
        this.idReserva   = idReserva;
        this.dataReserva = dataReserva;
        this.localizacoes= new ArrayList<>(localizacoes);
    }

    /**
     * Da serialize do objeto
     * @param out ObjectOutputStream onde o objeto vai ser escrito
     */
    public void serialize (ObjectOutputStream out) throws IOException {
        out.writeInt(this.idReserva);
        out.writeObject(this.dataReserva);
        out.writeInt(this.localizacoes.size());
        for (String localizacao:this.localizacoes){
            out.writeUTF(localizacao);
        }
    }

    /**
     * Da desserialize ao object input stream para obter o objeto
     * @param in ObjectInputStream de onde vai ler o objeto
     * @return Objeto
     */
    public static InformacaoSobreReserva deserialize (ObjectInputStream in) throws IOException, ClassNotFoundException {
        List<String> localizacoes = new ArrayList<>();

        int idReserva = in.readInt();
        LocalDate dataReserva = (LocalDate) in.readObject();
        int length = in.readInt();
        for (int i=0;i<length;i++){
            localizacoes.add(in.readUTF());
        }
        return new InformacaoSobreReserva(idReserva,dataReserva,localizacoes);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("Sequencia de voos da reserva com o id \""+this.idReserva+"\" do dia " + this.dataReserva+ "\n    ");
        int i;
        for (i=0;i<this.localizacoes.size()-1;i++) {
            sb.append(this.localizacoes.get(i)).append(" -> ");
        }
        sb.append(this.localizacoes.get(i)).append("\n");
        return sb.toString();
    }

    public int getIdReserva() {
        return idReserva;
    }

    public LocalDate getDataReserva() {
        return dataReserva;
    }

    public List<String> getLocalizacoes() {
        return new ArrayList<>(localizacoes);
    }
}
