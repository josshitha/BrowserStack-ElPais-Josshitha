package com.elpais.automation.services;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.elpais.automation.config.Constants;
import java.io.IOException;

import static com.elpais.automation.config.Constants.AWS_ACCESS_KEY;
import static com.elpais.automation.config.Constants.AWS_SECRET_KEY;

public class TranslationService {
    private final AmazonTranslate translateClient;

    public TranslationService() {
        this.translateClient = AmazonTranslateClient.builder()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                AWS_ACCESS_KEY,
                                AWS_SECRET_KEY
                        )
                ))
                .withRegion(Constants.AWS_REGION)
                .build();
    }

    public String translateSpanishToEnglish(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        try {
            TranslateTextRequest request = new TranslateTextRequest()
                    .withText(text)
                    .withSourceLanguageCode("es")
                    .withTargetLanguageCode("en");

            TranslateTextResult result = translateClient.translateText(request);
            return result.getTranslatedText();
        } catch (Exception e) {
            throw new IOException("AWS Translation failed: " + e.getMessage(), e);
        }
    }
}