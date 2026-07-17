package com.antigravity.malayalam.service;

import android.os.Handler;
import android.os.Looper;

import com.antigravity.malayalam.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContentGenerator {
    private final GenerativeModelFutures model;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public ContentGenerator() {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newSingleThreadExecutor();

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("MOCK_KEY")) {
            this.model = null; // Mock mode
        } else {
            GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
            this.model = GenerativeModelFutures.from(gm);
        }
    }

    public String buildPromptForBeginner() {
        return "Generate 5 simple Malayalam sentences with English translations for a beginner. " +
               "Output each sentence on a new line in the format: Malayalam - English. " +
               "Do not include any other text or markdown.";
    }

    public interface ContentCallback {
        void onSuccess(List<String> sentences);
        void onError(Throwable throwable);
    }

    public void generateBeginnerSentences(ContentCallback callback) {
        if (model == null) {
            // Mock mode
            executor.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    mainHandler.post(() -> callback.onError(e));
                    return;
                }
                List<String> mock = Arrays.asList(
                        "നമസ്കാരം - Hello",
                        "എനിക്ക് സുഖമാണ് - I am fine",
                        "നിങ്ങളുടെ പേര് എന്താണ്? - What is your name?",
                        "നന്ദി - Thank you",
                        "വിട - Goodbye"
                );
                mainHandler.post(() -> callback.onSuccess(mock));
            });
            return;
        }

        Content content = new Content.Builder()
                .addPart(new TextPart(buildPromptForBeginner()))
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                List<String> sentences = new ArrayList<>();
                if (text != null) {
                    for (String line : text.split("\n")) {
                        if (!line.trim().isEmpty()) {
                            sentences.add(line.trim());
                        }
                    }
                }
                mainHandler.post(() -> callback.onSuccess(sentences));
            }

            @Override
            public void onFailure(Throwable t) {
                mainHandler.post(() -> callback.onError(t));
            }
        }, executor);
    }
}
