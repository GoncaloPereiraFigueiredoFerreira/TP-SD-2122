package Desmultiplexer;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Testes {
    public static void main(String[] args) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);

        String s1 = "Alex", s2 = "Luis";
        objectOutputStream.writeUTF(s1);
        objectOutputStream.writeUTF(s2);
        objectOutputStream.flush();

        byte[] byteArray = baos.toByteArray();


        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        ObjectInputStream ois     = new ObjectInputStream(bais);

        System.out.println(ois.readUTF());
        System.out.println(ois.readUTF());
    }
}
