package Demultiplexer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Viagens{

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
