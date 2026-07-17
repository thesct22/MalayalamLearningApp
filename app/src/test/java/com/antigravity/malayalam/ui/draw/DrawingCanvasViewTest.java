package com.antigravity.malayalam.ui.draw;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import java.lang.reflect.Field;

public class DrawingCanvasViewTest {
    @Test
    public void testTemplatesContainVowels() throws Exception {
        Field field = DrawingCanvasView.class.getDeclaredField("TEMPLATES");
        field.setAccessible(true);
        Map<String, float[][]> templates = (Map<String, float[][]>) field.get(null);
        
        assertNotNull(templates);
        assertTrue("Should contain അ", templates.containsKey("അ"));
        assertTrue("Should contain ആ", templates.containsKey("ആ"));
        assertTrue("Should contain ഇ", templates.containsKey("ഇ"));
        assertTrue("Should contain ഈ", templates.containsKey("ഈ"));
        assertTrue("Should contain ഉ", templates.containsKey("ഉ"));
    }
}
