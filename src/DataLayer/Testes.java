package DataLayer;

import DataLayer.Voo;

import java.util.*;

public class Testes {

    //Tem readLock para ver o grafo e o map de voos
    private static void teste(HashMap<String,List<String>> map, String nodoAtual, String nodoFinal, int depthAtual, int depthMax, List<List<String>> listaDeCaminhos, List<String> caminhoAtual){
        List<String> l_voos = map.get(nodoAtual); //Lista dos ids dos voos que partem do nodo inicial

        if (l_voos != null && !caminhoAtual.contains(nodoAtual)) {
            caminhoAtual = new ArrayList<>(caminhoAtual);
            caminhoAtual.add(nodoAtual);

            for (String idVoo : l_voos) {
                //Adiciona o caminho até o nodo Atual caso encontre o destino
                if (idVoo.equals(nodoFinal)) {
                    List<String> novoCaminho = new ArrayList<>(caminhoAtual);
                    novoCaminho.add(nodoFinal);
                    listaDeCaminhos.add(novoCaminho);
                }
                //Se ainda não tiver encontrado o nodo final e não tiver atingido a profundidade máxima continua a procurar
                else if (depthAtual < depthMax)
                    teste(map, idVoo, nodoFinal, depthAtual + 1, depthMax, listaDeCaminhos, caminhoAtual);
            }
        }
    }

    //TODO - passar esta versao que está a funcionar
    private static void travessiaEmProfundidade(HashMap<String,List<String>> map, String nodoAtual, int depthAtual, int depthMax, List<List<String>> listaDeCaminhos, List<String> caminhoAtual) {
        List<String> l_voos = map.get(nodoAtual);
        //System.out.println("NodoAtual:" + nodoAtual + " | l_voos: " + l_voos + " | caminhoAtual: " + caminhoAtual);

        if(!caminhoAtual.contains(nodoAtual)) {
            caminhoAtual = new ArrayList<>(caminhoAtual);
            caminhoAtual.add(nodoAtual);

            if(caminhoAtual.size() > 1)
                listaDeCaminhos.add(new ArrayList<>(caminhoAtual));

            if (l_voos != null)
                for (String idVoo : l_voos)
                    if (depthAtual <= depthMax)
                        travessiaEmProfundidade(map, idVoo, depthAtual + 1, depthMax, listaDeCaminhos, caminhoAtual);
        }
    }

    //TODO - tentar otimizar. Nao ta a dar pq o caminho atual ao repetir n deixa entrar no if para fazer a escrita para a lista
    //TODO - Maybe adicionar nocao de nodos visitados
    /*private static void travessiaEmProfundidade2(HashMap<String,List<String>> map, String nodoAtual, int depthAtual, int depthMax, List<List<String>> listaDeCaminhos, List<String> caminhoAtual) {
        List<String> l_voos = map.get(nodoAtual);
        //System.out.println("NodoAtual:" + nodoAtual + " | l_voos: " + l_voos + " | caminhoAtual: " + caminhoAtual);

        if(!caminhoAtual.contains(nodoAtual)) {
            caminhoAtual = new ArrayList<>(caminhoAtual);
            caminhoAtual.add(nodoAtual);

            if (l_voos != null)
                for (String idVoo : l_voos) {
                    if (depthAtual <= depthMax)
                        travessiaEmProfundidade2(map, idVoo, depthAtual + 1, depthMax, listaDeCaminhos, caminhoAtual);
                }

            if ((l_voos == null || depthAtual == depthMax) && caminhoAtual.size() > 1)
                addSubListas(caminhoAtual, listaDeCaminhos);
        }
        else {
            addSubListas(caminhoAtual, listaDeCaminhos);
        }

    }*/

    private static void addSubListas(List<String> l, List<List<String>> listaOndeAdicionar){
       // listaOndeAdicionar.add(l.subList())
        for (int i = l.size(); i > 1; i--)
            listaOndeAdicionar.add(new ArrayList<>(l.subList(0,i)));
    }

    public static List<List<String>> listaViagensExistentes(HashMap<String,List<String>> map){
        List<List<String>> listaViagensExistentes = new ArrayList<>();
        Set<String> setOrigens = map.keySet(); //Set das origens dos voos

        for (String origem : setOrigens)
            travessiaEmProfundidade(map, origem, 1, 3, listaViagensExistentes, new ArrayList<>());

        return listaViagensExistentes;
    }


    public static void treeSetTester(TreeSet<Voo> set, String destino){

        if(set != null) {
            Voo voo = set.floor(Voo.vooParaComparacao(destino));
            if(voo != null && voo.getDestino().equals(destino)) System.out.println("Origem: " + voo.getIdVoo() + " | origem: " + voo.getOrigem() + " | destino: " + voo.getDestino() + " | cap: " + voo.getCapacidade());
        }


    }

    public static void main(String[] args) {
        /*HashMap<String,List<String>> map = new HashMap<>();

        List<String> lisboa = new ArrayList<>();
        lisboa.add("Braga"); lisboa.add("Porto"); lisboa.add("Guimaraes"); lisboa.add("Guarda");
        map.put("Lisboa",lisboa);

        List<String> braga = new ArrayList<>();
        braga.add("Porto");
        map.put("Braga",braga);

        List<String> guimaraes = new ArrayList<>();
        guimaraes.add("Braga"); guimaraes.add("Guarda");
        map.put("Guimaraes",guimaraes);

        List<String> Guarda = new ArrayList<>();
        Guarda.add("Guimaraes");
        map.put("Guarda",Guarda);

        List<List<String>> listaDeCaminhos = new ArrayList<>();
        teste(map, "Lisboa", "Porto", 1, 3,  listaDeCaminhos, new ArrayList<>());
        System.out.println(listaDeCaminhos);

        //Travessia
        List<List<String>> listaDeCaminhos2 = listaViagensExistentes(map);
        System.out.println(listaDeCaminhos2 + "\nsize: " + listaDeCaminhos2.size());*/

        TreeSet<Voo> voos = new TreeSet<>();
        voos.add(new Voo("Lisboa","Porto",1));
        voos.add(new Voo("Braga","Guimaraes",1));
        voos.add(new Voo("Guarda","Viseu",1));
        voos.add(new Voo("Algarve","Sagres",1));

        System.out.println(voos);

        treeSetTester(voos, "Porto");

        System.out.println(voos);
    }
}
