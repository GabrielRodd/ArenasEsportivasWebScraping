package com.scraper;

import com.model.ArenaModel;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class MapsScraper {

    public String buscarCidadePelaAPI(String enderecoCompleto) {
        try {
            // 1. Limpeza básica: O Google às vezes traz códigos no início.
            // Vamos tentar pegar apenas o que vem antes do CEP ou focar no final.
            String enderecoParaBusca = enderecoCompleto;
            if (enderecoCompleto.contains(",")) {
                // Pega as últimas partes (Geralmente: Bairro, Cidade - Estado)
                String[] partes = enderecoCompleto.split(",");
                if (partes.length > 2) {
                    enderecoParaBusca = partes[partes.length - 2] + "," + partes[partes.length - 1];
                }
            }

            String enderecoEncoded = URLEncoder.encode(enderecoParaBusca, StandardCharsets.UTF_8);
            String urlString = "https://nominatim.openstreetmap.org/search?q=" + enderecoEncoded + "&format=json&addressdetails=1&limit=1";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "MeuScraperEsportivo/1.0");

            if (conn.getResponseCode() == 200) {
                Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
                StringBuilder sb = new StringBuilder();
                while (sc.hasNext()) {
                    sb.append(sc.nextLine());
                }
                sc.close();

                String json = sb.toString();

                // 2. Melhorando a captura: O Nominatim varia muito o nome do campo
                String[] camposCidade = {"\"city\":\"", "\"town\":\"", "\"village\":\"", "\"municipality\":\"", "\"county\":\""};

                for (String campo : camposCidade) {
                    if (json.contains(campo)) {
                        return json.split(campo)[1].split("\"")[0];
                    }
                }

                // Se não achou cidade, mas achou o endereço, imprime o JSON pra gente debugar
                System.out.println("JSON recebido mas cidade não filtrada: " + json);
            }
        } catch (Exception e) {
            System.out.println("Erro na API: " + e.getMessage());
        }
        return "Cidade não identificada";
    }

    public List<ArenaModel> buscarArenaModel(String cidade) {
        List<ArenaModel> ArenasEncontradas = new ArrayList<>();

        // Configuração do Driver
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless"); // Descomente para rodar sem abrir a janela
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Acessa o Maps com a busca
        String termoBusca = "Arenas esportivas em " + cidade;
        driver.get("https://www.google.com/maps/search/" + termoBusca.replace(" ", "+"));

        // Navega pela div de resultados
        WebElement painelDeResultados = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@role='feed']")));

        for (int i = 0; i < 1; i++) { // Faz 5 scrolls
            painelDeResultados.sendKeys(Keys.PAGE_DOWN);

            // Pausa para o google carregar
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("A espera foi interrompida.");
            }
        }

        List<WebElement> linksEstabelecimentos = driver.findElements(By.className("hfpxzc"));

        // Salva os urls
        List<String> urlArenas = new ArrayList<>();

        for (WebElement link:linksEstabelecimentos) {
            String url = link.getAttribute("href");
            urlArenas.add(url);
        }

        // Acessando os atributos
        for (String url:urlArenas) {
            driver.get(url);

            //Extrair nome
            try {
                WebElement nomeArenaGoogle = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[@class='DUwDvf lfPIob']")));
                String nomeArena = nomeArenaGoogle.getText();
                ArenaModel arenaObj = new ArenaModel();
                arenaObj.setNome(nomeArena);

                //Extrair telefone
                try {
                    WebElement numeroArenaGoogle = driver.findElement(By.xpath("//div[contains(@class, 'Io6YTe') and starts-with(text(), '(')]"));
                    String telefone = numeroArenaGoogle.getText();
                    arenaObj.setNumero(telefone);


                }  catch (Exception e) {
                    arenaObj.setNumero("nao informado");
                }

                //Extrair endereco e cidade
                try {
                    WebElement enderecoArenaGoogle = driver.findElement(By.xpath("//button[contains(@aria-label, 'Endereço')]//div[contains(@class, 'Io6YTe')]"));
                    String enderecoArena = enderecoArenaGoogle.getText();
                    arenaObj.setEndereco(enderecoArena);

                    //Define cidade
                    String cidadeReal = buscarCidadePelaAPI(enderecoArena);
                    arenaObj.setCidade(cidadeReal);

                } catch (Exception e) {
                    arenaObj.setEndereco("nao informado");
                }

            //Adiciona arena na lista de arenas
            ArenasEncontradas.add(arenaObj);

            } catch (Exception e) {
                System.out.println("Erro ao processar URL: " + url);
            }
        }

        //APENAS PARA TESTAR SE ESTA EXTRAINDO CORRETAMENTE
        for (ArenaModel arena:ArenasEncontradas) {
            System.out.println(arena);
        }

        return ArenasEncontradas;
    }
}