package DataLayer;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InformacaoSobreReserva implements Serializable {
    int idReserva;
    LocalDate dataReserva;
    List<String> localizacoes;

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
     * @return Array de bytes com a informacao sobre o objeto
     */
    public byte[] serialize () throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeInt(this.idReserva);
        oos.writeObject(this.dataReserva);
        oos.writeInt(this.localizacoes.size());
        for (String localizacao:this.localizacoes){
            oos.writeUTF(localizacao);
        }
        oos.flush();
        byte[] byteArray = baos.toByteArray();

        oos.close();
        baos.close();

        return byteArray;
    }

    /**
     * Da desserialize a um byte[] para obter uma lista de listas de strings
     * @param bytes Array de bytes com a informacao sobre a viagens
     * @return Viagens que se pretende obter
     */
    public static InformacaoSobreReserva deserialize (byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        List<String> localizacoes = new ArrayList<>();

        int idReserva = ois.readInt();
        LocalDate dataReserva = (LocalDate) ois.readObject();
        int length = ois.readInt();
        for (int i=0;i<length;i++){
            localizacoes.add(ois.readUTF());
        }

        ois.close();
        bais.close();

        return new InformacaoSobreReserva(idReserva,dataReserva,localizacoes);
    }
}
