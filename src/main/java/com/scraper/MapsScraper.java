package com.scraper;

import com.model.ArenaModel;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MapsScraper {

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

            try {
                //Extrair nome
                WebElement nomeArenaGoogle = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[@class='DUwDvf lfPIob']")));
                String nomeArena = nomeArenaGoogle.getText();
                ArenaModel arenaObj = new ArenaModel();
                arenaObj.setNome(nomeArena);

                //Extrair telefone
                try {
                    WebElement numeroArenaGoogle = driver.findElement(By.xpath("//div[contains(@class, 'Io6YTe') and starts-with(text(), '(')]"));
                    String telefone = numeroArenaGoogle.getText();
                    arenaObj.setNumero(telefone);

                    ArenasEncontradas.add(arenaObj);

                }  catch (Exception e) {
                    arenaObj.setNumero("nao informado");
                    System.out.println("Telefone nao encontrado para essa arena");
                    ArenasEncontradas.add(arenaObj);
                }

            } catch (Exception e) {
                System.out.println("Erro ao processar URL: " + url);
            }
        }

        //APENAS PARA TESTAR SE ESTA EXTRAINDO CORRETAMENTE
        for (ArenaModel arena:ArenasEncontradas) {
            System.out.println(arena.getNome());
            System.out.println(arena.getNumero());
        }



        return ArenasEncontradas;
    }
}