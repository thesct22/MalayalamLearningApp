package com.antigravity.malayalam.service;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;

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

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey != null ? apiKey : "");
        this.model = GenerativeModelFutures.from(gm);
    }

    @VisibleForTesting
    ContentGenerator(GenerativeModelFutures model, ExecutorService executor, Handler mainHandler) {
        this.model = model;
        this.executor = executor;
        this.mainHandler = mainHandler;
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

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
