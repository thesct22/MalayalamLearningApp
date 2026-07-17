package com.antigravity.malayalam.ui.draw;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class DrawingCanvasViewTest {
    @Test
    public void testTemplatesContainVowels() throws Exception {
        Field field = LetterTemplates.class.getDeclaredField("CORE_VOWELS");
        field.setAccessible(true);
        String[] vowels = (String[]) field.get(null);
        
        assertNotNull(vowels);
        List<String> vowelList = Arrays.asList(vowels);
        assertTrue("Should contain അ", vowelList.contains("അ"));
        assertTrue("Should contain ആ", vowelList.contains("ആ"));
        assertTrue("Should contain ഇ", vowelList.contains("ഇ"));
        assertTrue("Should contain ഈ", vowelList.contains("ഈ"));
        assertTrue("Should contain ഉ", vowelList.contains("ഉ"));
    }
}
