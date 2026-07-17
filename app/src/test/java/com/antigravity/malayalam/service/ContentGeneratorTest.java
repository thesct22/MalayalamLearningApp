package com.antigravity.malayalam.service;

import android.os.Handler;
import android.os.Looper;

import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ContentGeneratorTest {
    private ExecutorService executor;
    private Handler mainHandler;
    private GenerativeModelFutures mockModel;
    private ContentGenerator generator;

    @Before
    public void setUp() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        mockModel = Mockito.mock(GenerativeModelFutures.class);
        generator = new ContentGenerator(mockModel, executor, mainHandler);
    }

    @After
    public void tearDown() {
        if (generator != null) {
            generator.shutdown();
        }
    }

    @Test
    public void testPromptGeneration() {
        String prompt = generator.buildPromptForBeginner();
        assertTrue(prompt.contains("Generate 5 simple Malayalam sentences"));
    }

    @Test
    public void testGenerateBeginnerSentences() throws InterruptedException {
        // mock the future
        GenerateContentResponse mockResponse = Mockito.mock(GenerateContentResponse.class);
        Mockito.when(mockResponse.getText()).thenReturn("നമസ്കാരം - Hello\nഎനിക്ക് സുഖമാണ് - I am fine\n");
        ListenableFuture<GenerateContentResponse> future = Futures.immediateFuture(mockResponse);
        Mockito.when(mockModel.generateContent(Mockito.any(Content.class))).thenReturn(future);

        final java.util.List<String>[] result = new java.util.List[1];

        generator.generateBeginnerSentences(new ContentGenerator.ContentCallback() {
            @Override
            public void onSuccess(java.util.List<String> sentences) {
                result[0] = sentences;
            }

            @Override
            public void onError(Throwable throwable) {
            }
        });

        // Poll the main looper to process messages sent from background threads
        long startTime = System.currentTimeMillis();
        while (result[0] == null && System.currentTimeMillis() - startTime < 3000) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper();
            Thread.sleep(100);
        }

        assertNotNull("Result should not be null", result[0]);
        assertFalse("Sentences list should not be empty", result[0].isEmpty());
        assertTrue(result[0].get(0).contains("നമസ്കാരം"));
    }
}
