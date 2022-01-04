package LogicLayer.ClassesSerializable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Viagens{

    /**
     * Da serialize a uma lista de listas de strings (viagens)
     * @param viagens Viagens que se pretende serializar
     * @return Array de bytes com a informacao sobre a viagens
     */
    public static byte[] serialize (List<List<String>> viagens) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeInt(viagens.size());
        for (List<String> list:viagens){
            oos.writeInt(list.size());
            for (String s: list){
                oos.writeUTF(s);
            }
        }
        oos.flush();
        byte[] byteArray = baos.toByteArray();

        oos.close();
        baos.close();

        return byteArray;
    }

    /**
     * Da desserialize a um byte[] para obter uma lista de listas de strings (viagens)
     * @param bytes Array de bytes com a informacao sobre a viagens
     * @return Viagens que se pretende obter
     */
    public static List<List<String>> deserialize (byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        List<List<String>> cl = new ArrayList<>();
        int length = ois.readInt();

        for (int i=0;i<length;i++){
            List<String> lista = new ArrayList<>();
            int tam = ois.readInt();
            for (int j=0;j<tam;j++){
                lista.add(ois.readUTF());
            }
            cl.add(lista);
        }

        ois.close();
        bais.close();

        return cl;
    }

    /**
     * Gera string com a origem e destino mais a informacao das viagens fornecidas
     * @return String com as informações relevantes sobre as viagens
     */
    public static String toStringOutput(List<List<String>> viagens, String origem, String destino) {
        StringBuilder sb = new StringBuilder("Lista de viagens entre " + origem + " e " + destino + "\n");

        for (List<String> list:viagens){
            sb.append("Voos: ");

            int i;
            for (i=0;i<list.size()-1;i++){
                sb.append(list.get(i)).append(" -> ");
            }
            sb.append(list.get(i)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Gera string com a informacao das viagens fornecidas
     * @return String com as informações relevantes sobre as viagens
     */
    public static String toStringOutput(List<List<String>> viagens) {
        StringBuilder sb = new StringBuilder("Lista de voos/viagens possiveis \n");
        for (List<String> list:viagens){
            sb.append("Voo: ");
            int i;
            for (i=0;i<list.size()-1;i++){
                sb.append(list.get(i)).append(" -> ");
            }
            sb.append(list.get(i)).append("\n");
        }
        return sb.toString();
    }
}
