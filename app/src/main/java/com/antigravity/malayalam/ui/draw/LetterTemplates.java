package com.antigravity.malayalam.ui.draw;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Typeface;

import androidx.annotation.FontRes;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates normalized Malayalam glyph-outline templates.
 *
 * Data structure:
 *
 * float[][][]
 *   [contour][point][x/y]
 *
 * A glyph can contain multiple contours, so float[][] is insufficient.
 */
public final class LetterTemplates {

    private static final String[] CORE_VOWELS = {
            "അ", "ആ", "ഇ", "ഈ",
            "ഉ", "ഊ", "ഋ", "ഌ",
            "എ", "ഏ", "ഐ",
            "ഒ", "ഓ", "ഔ"
    };

    /**
     * Optional characters. Include these only if your curriculum needs them.
     *
     * ൟ = archaic ii
     * ൠ = vocalic rr
     * ൡ = vocalic ll
     */
    private static final String[] ADDITIONAL_VOWELS = {
            "ൟ", "ൠ", "ൡ"
    };

    private static final Map<String, float[][][]> TEMPLATES =
            new LinkedHashMap<>();

    private static boolean initialized;

    private LetterTemplates() {
        // Utility class.
    }

    /**
     * Call once, preferably from Application.onCreate().
     */
    public static synchronized void initialize(
            Context context,
            @FontRes int fontResource,
            boolean includeAdditionalVowels
    ) {
        if (initialized) {
            return;
        }

        Typeface typeface = ResourcesCompat.getFont(context, fontResource);

        if (typeface == null) {
            throw new IllegalStateException(
                    "Unable to load Malayalam font resource: " + fontResource
            );
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(typeface);

        // A large size gives PathMeasure enough precision.
        paint.setTextSize(1000f);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);

        for (String vowel : CORE_VOWELS) {
            TEMPLATES.put(vowel, createOutlineTemplate(vowel, paint));
        }

        if (includeAdditionalVowels) {
            for (String vowel : ADDITIONAL_VOWELS) {
                TEMPLATES.put(vowel, createOutlineTemplate(vowel, paint));
            }
        }

        initialized = true;
    }

    /**
     * Returns:
     *
     * template[contourIndex][pointIndex][0] = normalized x
     * template[contourIndex][pointIndex][1] = normalized y
     */
    @Nullable
    public static float[][][] getTemplate(String letter) {
        ensureInitialized();
        return TEMPLATES.get(letter);
    }

    public static boolean contains(String letter) {
        ensureInitialized();
        return TEMPLATES.containsKey(letter);
    }

    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "LetterTemplates.initialize(...) must be called first."
            );
        }
    }

    private static float[][][] createOutlineTemplate(
            String text,
            Paint paint
    ) {
        Path glyphPath = new Path();

        paint.getTextPath(
                text,
                0,
                text.length(),
                0f,
                0f,
                glyphPath
        );

        RectF bounds = new RectF();
        glyphPath.computeBounds(bounds, true);

        if (bounds.isEmpty()
                || bounds.width() <= 0f
                || bounds.height() <= 0f) {
            return new float[0][][];
        }

        /*
         * Normalized output range:
         *
         * x = 0..1
         * y = 0..1
         *
         * A small margin prevents the glyph touching the view edges.
         */
        final float padding = 0.06f;
        final float availableSize = 1f - (padding * 2f);

        float scale = Math.min(
                availableSize / bounds.width(),
                availableSize / bounds.height()
        );

        float normalizedWidth = bounds.width() * scale;
        float normalizedHeight = bounds.height() * scale;

        float offsetX = (1f - normalizedWidth) / 2f;
        float offsetY = (1f - normalizedHeight) / 2f;

        /*
         * At textSize 1000, a spacing of about 7 pixels creates a
         * reasonably detailed polygon without producing thousands
         * of points.
         */
        final float sampleSpacing = 7f;

        PathMeasure measure = new PathMeasure(glyphPath, false);
        List<float[][]> contours = new ArrayList<>();

        float[] position = new float[2];

        do {
            float length = measure.getLength();

            if (length <= 0f) {
                continue;
            }

            int sampleCount = Math.max(
                    16,
                    (int) Math.ceil(length / sampleSpacing)
            );

            /*
             * Font outlines are normally closed contours.
             * Avoid repeating the first point at the end.
             */
            boolean closed = measure.isClosed();
            int pointCount = closed
                    ? sampleCount
                    : sampleCount + 1;

            float[][] points = new float[pointCount][2];

            for (int i = 0; i < pointCount; i++) {
                float fraction;

                if (closed) {
                    fraction = i / (float) sampleCount;
                } else {
                    fraction = i / (float) (pointCount - 1);
                }

                float distance = length * fraction;

                if (!measure.getPosTan(distance, position, null)) {
                    continue;
                }

                points[i][0] =
                        offsetX + ((position[0] - bounds.left) * scale);

                points[i][1] =
                        offsetY + ((position[1] - bounds.top) * scale);
            }

            contours.add(points);

        } while (measure.nextContour());

        return contours.toArray(new float[contours.size()][][]);
    }
}
