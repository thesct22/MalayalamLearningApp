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
}
