package Demultiplexer.ClassesSerializable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InformacaoSobreReserva implements Serializable {
    int idReserva;
    LocalDate dataReserva;
    List<String> localizacoes;

    public InformacaoSobreReserva(int idReserva,LocalDate dataReserva,List<String> localizacoes){
        this.idReserva=idReserva;
        this.dataReserva=dataReserva;
        this.localizacoes= new ArrayList<>(localizacoes);
    }
    public InformacaoSobreReserva(int idReserva,LocalDate dataReserva){
        this.idReserva=idReserva;
        this.dataReserva=dataReserva;
        this.localizacoes= new ArrayList<>();
    }
}
