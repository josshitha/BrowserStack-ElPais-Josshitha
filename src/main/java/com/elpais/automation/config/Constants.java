package com.elpais.automation.config;

import io.github.cdimascio.dotenv.Dotenv;


public class Constants {

    // To access env
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static final int TIMEOUT_SECONDS = 30;
    public static final int SHORT_WAIT = 5;
    public static final int LONG_WAIT = 10;
    // XPaths
    public static final String COOKIE_ACCEPT_XPATH =
            "//button[contains(translate(text(), 'ACCEPT', 'accept'), 'accept')] | " +
                    "//*[@id='didomi-notice-agree-button']";
    public static final String OPINION_SECTION_XPATH = "//*[@id=\"csw\"]/div[1]/nav/div/a[2]";
    public static final String ARTICLE_BASE_XPATH = "//*[@id='main-content']/div[1]/section//article";

    // Scraping Article Limits
    public static final int MAX_ARTICLES = 5;

    // Translation API
    public static final String AWS_ACCESS_KEY = getEnv("AWS_ACCESS_KEY");
    public static final String AWS_SECRET_KEY = getEnv("AWS_SECRET_KEY");
    public static final String AWS_REGION = "us-east-1";


    // Cover Image Download
    public static final String IMAGE_SAVE_DIR = "/Users/josshitha.s/Browserstack/ElPais-Automation/downloads/";

    // Browserstack Creds
    public static final String BROWSERSTACK_USERNAME = getEnv("BROWSERSTACK_USERNAME");
    public static final String BROWSERSTACK_ACCESS_KEY = getEnv("BROWSERSTACK_ACCESS_KEY");
    public static final String BROWSERSTACK_HUB_URL = "hub-cloud.browserstack.com/wd/hub";

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = dotenv.get(key);
        }
        if (value == null) {
            throw new IllegalStateException("Missing required environment variable");
        }
        return value;
    }
}



