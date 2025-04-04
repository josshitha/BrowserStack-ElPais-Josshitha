package com.elpais.automation.services;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.NoSuchElementException;
import com.elpais.automation.config.Constants;
import org.openqa.selenium.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ScraperService {
    public record Article(String title, String content, String imageUrl) {
        public String getTruncatedContent() {
            return content.length() > 100 ?
                    content.substring(0, 100) + "..." : content;
        }
    }
    private final WebDriver driver;
    public ScraperService(WebDriver driver) {
        this.driver = driver;
    }

    public List<Article> scrapeArticles() {
        List<Article> articles = new ArrayList<>();
        List<WebElement> articleContainers = driver.findElements(By.xpath(Constants.ARTICLE_BASE_XPATH));
        int totalArticles = Math.min(Constants.MAX_ARTICLES, articleContainers.size());
        for (int i = 0; i < totalArticles; i++) {
            try {
                WebElement article = articleContainers.get(i);
                String title = article.findElement(By.xpath(".//header//h2//a | .//header//h2")).getText();
                String content = article.findElement(By.xpath(".//p[1]")).getText();
                String imageUrl = getArticleImageUrl(article);
                articles.add(new Article(title, content, imageUrl));

            } catch (NoSuchElementException e) {
                System.out.println("Element missing in article");
            }
        }
        return articles;
    }

    private String getArticleImageUrl(WebElement article) {
        try {
            WebElement img = article.findElement(By.xpath(".//figure//img | .//picture//img"));
            String url = img.getAttribute("src");
            if (url != null && !url.isEmpty()) {
                downloadImage(url, "article_" + System.currentTimeMillis() + ".jpg");
                return url;
            }
        } catch (NoSuchElementException e) {
            System.out.println("No images found");
        }
        return "";
    }
    private void downloadImage(String imageUrl, String filename) {
        try {
            new File(Constants.IMAGE_SAVE_DIR).mkdirs();
            URL url = new URL(imageUrl);
            try (InputStream in = new URL(imageUrl).openStream();
                 FileOutputStream out = new FileOutputStream(Constants.IMAGE_SAVE_DIR + filename)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            System.out.println("Image download failed");
        }
    }
}