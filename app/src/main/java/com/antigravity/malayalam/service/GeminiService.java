package com.antigravity.malayalam.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.antigravity.malayalam.BuildConfig;
import com.antigravity.malayalam.data.LanguageTrack;
import com.antigravity.malayalam.data.VocabularyWord;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to interact with the Gemini API using official Google AI Java SDK.
 * Includes a robust fallback mechanism if the API key is not configured.
 */
public class GeminiService {

    private static final String TAG = "GeminiService";
    private static volatile GeminiService INSTANCE;
    private final GenerativeModelFutures model;
    private final boolean isMockMode;
    private final ExecutorService mockExecutor;
    private final Handler mainHandler;

    public interface GeminiCallback<T> {
        void onSuccess(T result);
        void onError(Throwable throwable);
    }

    private GeminiService() {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.mockExecutor = Executors.newSingleThreadExecutor();

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("MOCK_KEY")) {
            Log.w(TAG, "Gemini API Key not set or using MOCK_KEY. Running in Mock fallback mode.");
            this.isMockMode = true;
            this.model = null;
        } else {
            this.isMockMode = false;
            GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
            this.model = GenerativeModelFutures.from(gm);
        }
    }

    public static GeminiService getInstance() {
        if (INSTANCE == null) {
            synchronized (GeminiService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GeminiService();
                }
            }
        }
        return INSTANCE;
    }

    public void generateQuizzesForWeaknesses(List<VocabularyWord> weakWords, GeminiCallback<String> callback) {
        if (isMockMode) {
            runMockDelay(() -> callback.onSuccess(getMockQuizJson(weakWords)), callback);
            return;
        }

        StringBuilder wordsStr = new StringBuilder();
        if (weakWords != null) {
            for (VocabularyWord w : weakWords) {
                wordsStr.append(w.getMalayalamScript()).append(" (").append(w.getEnglishTranslation()).append("), ");
            }
        }

        String prompt = "Generate exactly 5 multiple-choice questions for Malayalam language learning targeting these weak words: " 
                + wordsStr.toString() 
                + ". Respond ONLY with a valid JSON array of objects. Do not include markdown code block syntax (like ```json), just raw JSON."
                + "Each object must have fields: 'malayalamScript' (String), 'phonetic' (String), 'englishTranslation' (String), 'chineseTranslation' (String), "
                + "'options' (array of 4 Strings in English or Chinese based on target), 'correctOptionIndex' (int 0-3), and 'type' (String: 'CLICK', 'LISTEN', 'SPEAK', or 'DRAW').";

        executePrompt(prompt, callback);
    }

    public void requestCustomLearningTrack(LanguageTrack track, GeminiCallback<String> callback) {
        if (isMockMode) {
            runMockDelay(() -> callback.onSuccess(getMockTrackJson(track)), callback);
            return;
        }

        String targetLang = track == LanguageTrack.ENGLISH_TO_MALAYALAM ? "English" : "Chinese";
        String prompt = "Generate a custom learning track containing 5 new vocabulary words for learning Malayalam from " + targetLang 
                + ". Respond ONLY with a valid JSON array of objects. Do not include markdown code block syntax (like ```json), just raw JSON."
                + "Each object must have fields: 'malayalamScript' (String), 'phonetic' (String), 'englishTranslation' (String), 'chineseTranslation' (String), "
                + "'difficultyLevel' (int 1-3), 'category' (String).";

        executePrompt(prompt, callback);
    }

    private void executePrompt(String prompt, GeminiCallback<String> callback) {
        try {
            Content.Builder builder = new Content.Builder();
            builder.setRole("user");
            builder.addPart(new TextPart(prompt));
            Content content = builder.build();
            ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);
            Futures.addCallback(responseFuture, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String jsonText = result.getText();
                    if (jsonText != null) {
                        // Strip markdown formatting if present
                        jsonText = jsonText.trim();
                        if (jsonText.startsWith("```json")) {
                            jsonText = jsonText.substring(7);
                        }
                        if (jsonText.endsWith("```")) {
                            jsonText = jsonText.substring(0, jsonText.length() - 3);
                        }
                        jsonText = jsonText.trim();
                    }
                    String finalJson = jsonText;
                    mainHandler.post(() -> callback.onSuccess(finalJson));
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Gemini API call failed, falling back to mock content", t);
                    // Resilient fallback to mock data on error
                    mainHandler.post(() -> callback.onSuccess(getMockFallbackJson()));
                }
            }, mockExecutor);
        } catch (Exception e) {
            Log.e(TAG, "Exception initializing prompt call", e);
            callback.onError(e);
        }
    }

    private void runMockDelay(Runnable runnable, GeminiCallback<?> callback) {
        mockExecutor.execute(() -> {
            try {
                Thread.sleep(1000); // Simulate network latency
                mainHandler.post(runnable);
            } catch (InterruptedException e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private String getMockQuizJson(List<VocabularyWord> weakWords) {
        return "[" +
                "  {" +
                "    \"malayalamScript\": \"നമസ്കാരം\"," +
                "    \"phonetic\": \"Namaskaram\"," +
                "    \"englishTranslation\": \"Hello\"," +
                "    \"chineseTranslation\": \"你好\"," +
                "    \"options\": [\"Goodbye\", \"Hello\", \"Thank you\", \"Water\"]," +
                "    \"correctOptionIndex\": 1," +
                "    \"type\": \"CLICK\"" +
                "  }," +
                "  {" +
                "    \"malayalamScript\": \"നന്ദി\"," +
                "    \"phonetic\": \"Nandi\"," +
                "    \"englishTranslation\": \"Thank you\"," +
                "    \"chineseTranslation\": \"谢谢\"," +
                "    \"options\": [\"Hello\", \"No\", \"Yes\", \"Thank you\"]," +
                "    \"correctOptionIndex\": 3," +
                "    \"type\": \"LISTEN\"" +
                "  }," +
                "  {" +
                "    \"malayalamScript\": \"വെള്ളം\"," +
                "    \"phonetic\": \"Vellam\"," +
                "    \"englishTranslation\": \"Water\"," +
                "    \"chineseTranslation\": \"水\"," +
                "    \"options\": [\"Water\", \"Food\", \"House\", \"Friend\"]," +
                "    \"correctOptionIndex\": 0," +
                "    \"type\": \"SPEAK\"" +
                "  }," +
                "  {" +
                "    \"malayalamScript\": \"അ\"," +
                "    \"phonetic\": \"A\"," +
                "    \"englishTranslation\": \"Letter 'A'\"," +
                "    \"chineseTranslation\": \"字母 'A'\"," +
                "    \"options\": [\"A\", \"Ka\", \"Ra\", \"Ta\"]," +
                "    \"correctOptionIndex\": 0," +
                "    \"type\": \"DRAW\"" +
                "  }," +
                "  {" +
                "    \"malayalamScript\": \"അമ്മ\"," +
                "    \"phonetic\": \"Amma\"," +
                "    \"englishTranslation\": \"Mother\"," +
                "    \"chineseTranslation\": \"母亲\"," +
                "    \"options\": [\"Father\", \"Mother\", \"Brother\", \"Sister\"]," +
                "    \"correctOptionIndex\": 1," +
                "    \"type\": \"CLICK\"" +
                "  }" +
                "]";
    }

    private String getMockTrackJson(LanguageTrack track) {
        return "[" +
                "  {" +
                "    \"malayalamScript\": \"സ്നേഹം\"," +
                "    \"phonetic\": \"Snehm\"," +
                "    \"englishTranslation\": \"Love\"," +
                "    \"chineseTranslation\": \"爱\"," +
                "    \"difficultyLevel\": 2," +
                "    \"category\": \"Feelings\"" +
                "  }," +
                "  {" +
                "    \"malayalamScript\": \"സന്തോഷം\"," +
                "    \"phonetic\": \"Santhosham\"," +
                "    \"englishTranslation\": \"Happiness\"," +
                "    \"chineseTranslation\": \"快乐\"," +
                "    \"difficultyLevel\": 2," +
                "    \"category\": \"Feelings\"" +
                "  }," +
                "  {" +
                "    \"malayalamScript\": \"സൂര്യൻ\"," +
                "    \"phonetic\": \"Sooryan\"," +
                "    \"englishTranslation\": \"Sun\"," +
                "    \"chineseTranslation\": \"太阳\"," +
                "    \"difficultyLevel\": 3," +
                "    \"category\": \"Nature\"" +
                "  }," +
                "  {" +
                "    \"malayalamScript\": \"ചന്ദ്രൻ\"," +
                "    \"phonetic\": \"Chandran\"," +
                "    \"englishTranslation\": \"Moon\"," +
                "    \"chineseTranslation\": \"月亮\"," +
                "    \"difficultyLevel\": 3," +
                "    \"category\": \"Nature\"" +
                "  }," +
                "  {" +
                "    \"malayalamScript\": \"ആകാശം\"," +
                "    \"phonetic\": \"Aakasham\"," +
                "    \"englishTranslation\": \"Sky\"," +
                "    \"chineseTranslation\": \"天空\"," +
                "    \"difficultyLevel\": 3," +
                "    \"category\": \"Nature\"" +
                "  }" +
                "]";
    }

    private String getMockFallbackJson() {
        return getMockQuizJson(null);
    }
}
