package com;

import com.model.ArenaModel;
import com.scraper.MapsScraper;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        MapsScraper scraper = new MapsScraper();

        List<ArenaModel> listaArenasTaubate = scraper.buscarArenaModel("taubate");








    }
}
