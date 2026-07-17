package com.antigravity.malayalam.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ContentGeneratorTest {
    @Test
    public void testPromptGeneration() {
        ContentGenerator generator = new ContentGenerator();
        String prompt = generator.buildPromptForBeginner();
        assertTrue(prompt.contains("Generate 5 simple Malayalam sentences"));
    }

    @Test
    public void testGenerateBeginnerSentences() throws InterruptedException {
        ContentGenerator generator = new ContentGenerator();
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        final java.util.List<String>[] result = new java.util.List[1];

        generator.generateBeginnerSentences(new ContentGenerator.ContentCallback() {
            @Override
            public void onSuccess(java.util.List<String> sentences) {
                result[0] = sentences;
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
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
