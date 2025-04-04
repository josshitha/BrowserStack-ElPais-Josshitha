package com.elpais.automation;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.*;

import com.elpais.automation.config.Constants;
import com.elpais.automation.services.ScraperService;
import com.elpais.automation.services.TranslationService;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.io.IOException;


public class ElPaisScraper {

    //To run locally

//    public static void main(String[] args) {
//        WebDriver driver = new ChromeDriver();
//        driver.manage().window().maximize();
//
//        try {
//            driver.get("https://elpais.com");
//            verifyLanguage(driver);
//            TranslationService translator = new TranslationService();
//            ScraperService scraper = new ScraperService(driver);
//            scraper.acceptCookies();
//            navigateToOpinionSection(driver);
//            List<ScraperService.Article> articles = scraper.scrapeArticles();
//            printResults(articles);
//            List<String> translatedTitlesList = printResultsWithTranslations(articles, translator);
//            countRepeatedWords(translatedTitlesList);
//
//        } finally {
//            driver.quit();
//        }
//    }

    //for parallel execution using browserstack
    WebDriver driver;
    public ElPaisScraper (WebDriver driver) {
        this.driver = driver;
    }
    public void executeTestFlow() throws IOException {

        this.driver.manage().window().maximize();

        try {
            this.driver.get("https://elpais.com");
            verifyLanguage(this.driver);
            TranslationService translator = new TranslationService();
            ScraperService scraper = new ScraperService(this.driver);
            acceptCookies(driver);
            navigateToOpinionSection(this.driver);
            List<ScraperService.Article> articles = scraper.scrapeArticles();
            printResults(articles);
            List<String> translatedTitlesList = printResultsWithTranslations(articles, translator);
            countRepeatedWords(translatedTitlesList);

        } finally {
            this.driver.quit();
        }
    }

    private static void verifyLanguage(WebDriver driver) {
        WebElement htmlTag = driver.findElement(By.tagName("html"));
        String langValue = htmlTag.getAttribute("lang");
        if ("es-ES".equals(langValue)) {
            System.out.println("El País is in Spanish");
        } else {
            System.out.println("El País is NOT in Spanish");
        }
    }
    private boolean acceptCookies(WebDriver driver) {
        try {
            WebElement cookieAccept = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath(Constants.COOKIE_ACCEPT_XPATH)));

            try {
                cookieAccept.click();
            } catch (Exception e) {
                ((JavascriptExecutor)driver).executeScript(
                        "arguments[0].click();", cookieAccept);
            }

            try { Thread.sleep(500); } catch (InterruptedException ignored) {}

            return true;
        } catch (Exception e) {
            System.out.println("Cookie acceptance not required or failed: " + e.getMessage());
            return false;
        }
    }
    private static void navigateToOpinionSection(WebDriver driver) {
        try {
            WebElement opinionLink = new WebDriverWait(driver, Duration.ofSeconds(Constants.LONG_WAIT))
                    .until(d -> {
                        WebElement el = driver.findElement(By.xpath(Constants.OPINION_SECTION_XPATH));
                        try {
                            el.click();
                            return el;
                        } catch (Exception e) {
                            ((JavascriptExecutor)driver).executeScript(
                                    "arguments[0].click();", el);
                            return el;
                        }
                    });

            new WebDriverWait(driver, Duration.ofSeconds(Constants.LONG_WAIT))
                    .until(ExpectedConditions.urlContains("opinion"));

        } catch (Exception e) {
            System.out.println("Navigation failed: " + e.getMessage());
            throw new RuntimeException("Couldn't navigate to opinion section", e);
        }
    }

    private static void printResults(List<ScraperService.Article> articles) {
        System.out.println("\nResults: " + articles.size() + " articles");

        for (ScraperService.Article article : articles) {
            System.out.println("\nTitle: " + article.title());
            System.out.println("Content: " + article.getTruncatedContent());
            System.out.println("Image: " +
                    (article.imageUrl().isBlank() ? "Not available" : "Downloaded"));
        }
    }

    private static List<String> printResultsWithTranslations(
            List<ScraperService.Article> articles,
            TranslationService translator) {
        List<String> translatedTitlesList = new ArrayList<>();
        for (ScraperService.Article article : articles) {
            try {
                System.out.println("\nOriginal Title: " + article.title());
                String translatedTitle = translator.translateSpanishToEnglish(article.title());
                translatedTitlesList.add(translatedTitle);
                System.out.println("Translated English Title: " + translatedTitle);

            } catch (IOException e) {
                System.err.println(e);
            }
        }
        return translatedTitlesList;
    }
    private static void countRepeatedWords(List<String> translatedTitlesList) {
        Map<String, Integer> wordCounts = new HashMap<>();
        for (String translatedTitle : translatedTitlesList) {
            wordCounts.put(translatedTitle, wordCounts.getOrDefault(translatedTitle, 0) + 1);
        }
        boolean foundRepeats = false;

        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            if (entry.getValue() > 2) {
                System.out.println("\n" + entry.getKey() + ": " + entry.getValue() + " times");
                foundRepeats = true;
            }
        }

        if (!foundRepeats) {
            System.out.println("\nNo words repeated more than twice");
        }
    }
}