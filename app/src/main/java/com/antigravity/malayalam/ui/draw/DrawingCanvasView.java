package com.antigravity.malayalam.ui.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom View class for drawing/tracing Malayalam letters.
 */
public class DrawingCanvasView extends View {

    private Path drawPath;
    private Paint drawPaint;
    private Paint backgroundLetterPaint;
    private Paint guidePointPaint;
    private Paint guideLinePaint;

    private String currentLetter = "അ";
    private final List<PointF> normalizedGuides = new ArrayList<>();
    private final List<PointF> scaledGuides = new ArrayList<>();
    private int nextGuideIndex = 0;
    private final float touchTolerance = 60f; // pixels distance threshold

    private DrawingListener listener;

    public interface DrawingListener {
        void onTracingProgress(int visitedPoints, int totalPoints);
        void onTracingCompleted();
        void onTracingFailed();
    }

    private static final Map<String, float[][]> TEMPLATES = new HashMap<>();
    static {
        // "അ" (a)
        TEMPLATES.put("അ", new float[][]{
            {0.35f, 0.35f}, {0.40f, 0.30f}, {0.45f, 0.35f}, {0.40f, 0.40f}, {0.35f, 0.35f}, // Starting loop
            {0.50f, 0.25f}, {0.65f, 0.25f}, {0.70f, 0.35f}, {0.65f, 0.45f}, {0.45f, 0.45f}, // Upper curve and middle bridge
            {0.30f, 0.55f}, {0.35f, 0.70f}, {0.50f, 0.75f}, {0.65f, 0.70f}, {0.70f, 0.55f}, // Lower curve and right sweep
            {0.55f, 0.55f}, {0.55f, 0.70f}, {0.70f, 0.75f}, {0.80f, 0.65f}, {0.80f, 0.45f}  // Inward hook and tail
        });
        // "ക" (ka)
        TEMPLATES.put("ക", new float[][]{
            {0.2f, 0.5f}, {0.3f, 0.3f}, {0.5f, 0.3f}, {0.6f, 0.5f}, 
            {0.5f, 0.7f}, {0.3f, 0.7f}, {0.5f, 0.5f}, {0.7f, 0.3f}, 
            {0.8f, 0.5f}, {0.7f, 0.7f}
        });
        // "റ" (ra)
        TEMPLATES.put("റ", new float[][]{
            {0.3f, 0.5f}, {0.4f, 0.3f}, {0.6f, 0.3f}, {0.7f, 0.5f}, 
            {0.6f, 0.7f}, {0.4f, 0.7f}
        });
    }

    public DrawingCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();

        drawPaint = new Paint();
        drawPaint.setColor(Color.parseColor("#6200EE")); // Deep Purple
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(16f);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        backgroundLetterPaint = new Paint();
        backgroundLetterPaint.setColor(Color.parseColor("#E0E0E0")); // Light Grey for template
        backgroundLetterPaint.setTextSize(400f);
        backgroundLetterPaint.setTextAlign(Paint.Align.CENTER);
        backgroundLetterPaint.setAntiAlias(true);
        backgroundLetterPaint.setStyle(Paint.Style.FILL);

        guidePointPaint = new Paint();
        guidePointPaint.setAntiAlias(true);
        guidePointPaint.setStyle(Paint.Style.FILL);

        guideLinePaint = new Paint();
        guideLinePaint.setColor(Color.parseColor("#8003DAC5")); // Semi-transparent teal
        guideLinePaint.setStrokeWidth(6f);
        guideLinePaint.setStyle(Paint.Style.STROKE);
        guideLinePaint.setAntiAlias(true);

        loadLetterTemplate(currentLetter);
    }

    public void setDrawingListener(DrawingListener listener) {
        this.listener = listener;
    }

    public void setLetter(String letter) {
        this.currentLetter = letter;
        loadLetterTemplate(letter);
        invalidate();
    }

    private void loadLetterTemplate(String letter) {
        normalizedGuides.clear();
        float[][] coords = TEMPLATES.get(letter);
        if (coords != null) {
            for (float[] coord : coords) {
                normalizedGuides.add(new PointF(coord[0], coord[1]));
            }
        } else {
            // Default template if letter not found: circular path
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4;
                float x = (float) (0.5 + 0.3 * Math.cos(angle));
                float y = (float) (0.5 + 0.3 * Math.sin(angle));
                normalizedGuides.add(new PointF(x, y));
            }
        }
        nextGuideIndex = 0;
        updateScaledGuides();
        clearCanvas();
    }

    private void updateScaledGuides() {
        scaledGuides.clear();
        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0) {
            for (PointF norm : normalizedGuides) {
                scaledGuides.add(new PointF(norm.x * width, norm.y * height));
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        backgroundLetterPaint.setTextSize(Math.min(w, h) * 0.7f);
        updateScaledGuides();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = getWidth() / 2;
        // Adjust baseline so character fits in middle
        int centerY = (int) ((getHeight() / 2) - ((backgroundLetterPaint.descent() + backgroundLetterPaint.ascent()) / 2));
        canvas.drawText(currentLetter, centerX, centerY, backgroundLetterPaint);

        // Draw connections between guide points
        if (scaledGuides.size() > 1) {
            for (int i = 0; i < scaledGuides.size() - 1; i++) {
                PointF p1 = scaledGuides.get(i);
                PointF p2 = scaledGuides.get(i + 1);
                // Highlight completed segments
                if (i < nextGuideIndex - 1) {
                    guideLinePaint.setColor(Color.parseColor("#FF03DAC5")); // Solid teal
                } else {
                    guideLinePaint.setColor(Color.parseColor("#30000000")); // Faint solid line
                }
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, guideLinePaint);
            }
        }

        // Draw current user strokes
        canvas.drawPath(drawPath, drawPaint);

        // Draw guide points
        for (int i = 0; i < scaledGuides.size(); i++) {
            PointF pt = scaledGuides.get(i);
            if (i < nextGuideIndex) {
                guidePointPaint.setColor(Color.parseColor("#4CAF50")); // Green: Visited
            } else if (i == nextGuideIndex) {
                guidePointPaint.setColor(Color.parseColor("#FF9800")); // Orange: Current Target
            } else {
                guidePointPaint.setColor(Color.parseColor("#757575")); // Grey: Unvisited
            }
            canvas.drawCircle(pt.x, pt.y, i == nextGuideIndex ? 22f : 16f, guidePointPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                checkTouchPoint(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                checkTouchPoint(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                // Check final status on release
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void checkTouchPoint(float x, float y) {
        if (nextGuideIndex >= scaledGuides.size()) return;

        PointF target = scaledGuides.get(nextGuideIndex);
        double dist = Math.hypot(x - target.x, y - target.y);

        if (dist <= touchTolerance) {
            nextGuideIndex++;
            if (listener != null) {
                listener.onTracingProgress(nextGuideIndex, scaledGuides.size());
                if (nextGuideIndex == scaledGuides.size()) {
                    listener.onTracingCompleted();
                }
            }
        }
    }

    public void clearCanvas() {
        drawPath.reset();
        nextGuideIndex = 0;
        invalidate();
    }

    public boolean isTracingSuccessful() {
        return nextGuideIndex >= scaledGuides.size();
    }
}
