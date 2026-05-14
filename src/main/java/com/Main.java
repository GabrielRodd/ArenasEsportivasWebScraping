package com;

import com.model.ArenaModel;
import com.scraper.MapsScraper;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        //Criando objeto scraper para usar os metodos da classe MapsScraper
        MapsScraper scraper = new MapsScraper();

        //Criando lista de cidades a serem extraidas
        List<String> listaCidades = new ArrayList<>();
        listaCidades.add("Taubate");
        listaCidades.add("Sao Jose dos Campos");
        //listaCidades.add("Pindamonhangaba");
        //listaCidades.add("Jacarei");
        //listaCidades.add("Cacapava");


        for (String cidade:listaCidades) {
            List<ArenaModel> listaArenasEsportivas = scraper.buscarArenaModel(cidade);
        }








    }
}
