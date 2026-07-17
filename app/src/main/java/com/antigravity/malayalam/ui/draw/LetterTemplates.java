package com.antigravity.malayalam.ui.draw;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the normalized float[][] coordinate templates for tracing Malayalam letters.
 * You can generate these coordinates using the admin_panel.html tool.
 */
public class LetterTemplates {

    public static final Map<String, float[][]> TEMPLATES = new HashMap<>();

    static {
        // "അ" (a)
        TEMPLATES.put("അ", new float[][]{
            {0.12f, 0.69f}, {0.06f, 0.63f}, {0.02f, 0.54f}, {0.02f, 0.49f}, {0.06f, 0.44f}, 
            {0.13f, 0.38f}, {0.20f, 0.36f}, {0.29f, 0.37f}, {0.37f, 0.38f}, {0.42f, 0.43f}, 
            {0.44f, 0.50f}, {0.38f, 0.53f}, {0.34f, 0.56f}, {0.41f, 0.58f}, {0.45f, 0.66f}, 
            {0.42f, 0.71f}, {0.36f, 0.71f}, {0.28f, 0.71f}, {0.25f, 0.64f}, {0.23f, 0.60f}, 
            {0.23f, 0.53f}, {0.23f, 0.47f}, {0.26f, 0.44f}, {0.30f, 0.39f}, {0.36f, 0.35f}, 
            {0.42f, 0.35f}, {0.49f, 0.37f}, {0.56f, 0.40f}, {0.59f, 0.45f}, {0.60f, 0.70f}, 
            {0.60f, 0.56f}, {0.64f, 0.42f}, {0.70f, 0.37f}, {0.77f, 0.37f}, {0.84f, 0.38f}, 
            {0.90f, 0.41f}, {0.96f, 0.48f}, {0.96f, 0.54f}, {0.95f, 0.62f}, {0.90f, 0.68f}, 
            {0.84f, 0.70f}, {0.77f, 0.68f}, {0.74f, 0.57f}, {0.76f, 0.51f}, {0.82f, 0.48f}, 
            {0.88f, 0.47f}, {0.92f, 0.51f}
        });
        
        // "ആ" (aa)
        TEMPLATES.put("ആ", new float[][]{
            {0.20f, 0.45f}, {0.30f, 0.25f}, {0.50f, 0.20f}, {0.70f, 0.25f}, {0.80f, 0.45f},
            {0.70f, 0.65f}, {0.50f, 0.70f}, {0.30f, 0.65f}, {0.20f, 0.45f},
            {0.20f, 0.70f}, {0.50f, 0.90f}, {0.80f, 0.90f},
            {0.90f, 0.70f}, {0.95f, 0.50f}
        });
        
        // "ഇ" (i)
        TEMPLATES.put("ഇ", new float[][]{
            {0.20f, 0.25f}, {0.40f, 0.25f}, {0.60f, 0.40f}, {0.40f, 0.60f}, {0.20f, 0.40f},
            {0.40f, 0.20f}, {0.70f, 0.20f}, {0.80f, 0.40f}, {0.70f, 0.60f}, {0.40f, 0.80f},
            {0.60f, 0.90f}, {0.80f, 0.90f}
        });
        
        // "ഈ" (ee)
        TEMPLATES.put("ഈ", new float[][]{
            {0.20f, 0.25f}, {0.40f, 0.25f}, {0.60f, 0.40f}, {0.40f, 0.60f}, {0.20f, 0.40f},
            {0.40f, 0.20f}, {0.70f, 0.20f}, {0.80f, 0.40f}, {0.70f, 0.60f}, {0.40f, 0.80f},
            {0.60f, 0.90f}, {0.80f, 0.90f},
            {0.90f, 0.70f}, {0.95f, 0.50f}
        });
        
        // "ഉ" (u)
        TEMPLATES.put("ഉ", new float[][]{
            {0.30f, 0.25f}, {0.50f, 0.20f}, {0.70f, 0.25f}, {0.80f, 0.45f},
            {0.70f, 0.65f}, {0.50f, 0.70f}, {0.30f, 0.65f},
            {0.30f, 0.80f}, {0.50f, 0.90f}, {0.80f, 0.90f}
        });
    }

    /**
     * Helper to get template coordinates for a letter, returns null if not found.
     */
    public static float[][] getTemplate(String letter) {
        return TEMPLATES.get(letter);
    }
}
