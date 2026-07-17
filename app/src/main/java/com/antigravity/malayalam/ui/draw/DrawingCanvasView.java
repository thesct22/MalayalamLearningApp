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
import java.util.List;

/**
 * Custom View class for drawing/tracing Malayalam letters using dynamic font templates.
 */
public class DrawingCanvasView extends View {

    private DrawingCanvasViewModel viewModel = new DrawingCanvasViewModel();

    private Path drawPath;
    private Paint drawPaint;
    private Paint templatePaint;
    private Paint templateStrokePaint;

    private String currentLetter = "അ";
    private float[][][] currentTemplate = null;
    private Path templatePath = new Path();
    private final List<PointF> scaledGuidePoints = new ArrayList<>();
    
    private float touchTolerance = 60f; // pixels distance threshold

    private DrawingListener listener;
    private boolean tracingSuccessful = false;

    public interface DrawingListener {
        void onTracingProgress(int visitedPoints, int totalPoints);
        void onTracingCompleted();
        void onTracingFailed();
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
        drawPaint.setStrokeWidth(24f); // Thicker stroke for handwriting
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        templatePaint = new Paint();
        templatePaint.setColor(Color.parseColor("#E0E0E0")); // Light Grey fill
        templatePaint.setAntiAlias(true);
        templatePaint.setStyle(Paint.Style.FILL);

        templateStrokePaint = new Paint();
        templateStrokePaint.setColor(Color.parseColor("#BDBDBD")); // Slightly darker grey outline
        templateStrokePaint.setStrokeWidth(3f);
        templateStrokePaint.setStyle(Paint.Style.STROKE);
        templateStrokePaint.setAntiAlias(true);

        loadLetterTemplate(currentLetter);
    }

    public void setDrawingListener(DrawingListener listener) {
        this.listener = listener;
    }

    public void setViewModel(DrawingCanvasViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setLetter(String letter) {
        this.currentLetter = letter;
        loadLetterTemplate(letter);
        invalidate();
    }

    private void loadLetterTemplate(String letter) {
        currentTemplate = LetterTemplates.getTemplate(letter);
        updateScaledGuides();
        clearCanvas();
    }

    private void updateScaledGuides() {
        scaledGuidePoints.clear();
        templatePath.reset();
        
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0 || currentTemplate == null) {
            return;
        }

        // Adjust for drawing within the view, preserving aspect ratio
        float scale = Math.min(width, height);
        float offsetX = (width - scale) / 2f;
        float offsetY = (height - scale) / 2f;
        
        touchTolerance = scale * 0.08f; // Dynamic tolerance based on view size

        for (float[][] contour : currentTemplate) {
            if (contour == null || contour.length == 0) continue;

            float startX = offsetX + contour[0][0] * scale;
            float startY = offsetY + contour[0][1] * scale;
            
            templatePath.moveTo(startX, startY);
            scaledGuidePoints.add(new PointF(startX, startY));

            for (int i = 1; i < contour.length; i++) {
                float px = offsetX + contour[i][0] * scale;
                float py = offsetY + contour[i][1] * scale;
                templatePath.lineTo(px, py);
                scaledGuidePoints.add(new PointF(px, py));
            }
            templatePath.close();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateScaledGuides();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw the extracted glyph as a bubble shape
        canvas.drawPath(templatePath, templatePaint);
        canvas.drawPath(templatePath, templateStrokePaint);

        // Draw user's stroke
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (viewModel != null) viewModel.addPoint(touchX, touchY);
                drawPath.moveTo(touchX, touchY);
                tracingSuccessful = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (viewModel != null) viewModel.addPoint(touchX, touchY);
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                evaluateTrace();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void evaluateTrace() {
        if (viewModel == null || scaledGuidePoints.isEmpty()) return;
        List<PointF> userPoints = viewModel.getPoints();
        if (userPoints.size() < 10) return; // Too short

        // 1. Accuracy: what % of user points are inside/near the template?
        int accuratePoints = 0;
        for (PointF up : userPoints) {
            boolean isNear = false;
            for (PointF gp : scaledGuidePoints) {
                if (Math.hypot(up.x - gp.x, up.y - gp.y) <= touchTolerance) {
                    isNear = true;
                    break;
                }
            }
            if (isNear) accuratePoints++;
        }
        float accuracy = (float) accuratePoints / userPoints.size();

        // 2. Coverage: what % of the template points were touched?
        int coveredGuides = 0;
        for (PointF gp : scaledGuidePoints) {
            boolean isCovered = false;
            for (PointF up : userPoints) {
                if (Math.hypot(up.x - gp.x, up.y - gp.y) <= touchTolerance) {
                    isCovered = true;
                    break;
                }
            }
            if (isCovered) coveredGuides++;
        }
        float coverage = (float) coveredGuides / scaledGuidePoints.size();

        float finalScore = (accuracy * 0.65f) + (coverage * 0.35f);
        
        // Let's say a score of > 0.65 is a pass
        tracingSuccessful = finalScore >= 0.65f;

        if (listener != null) {
            if (tracingSuccessful) {
                listener.onTracingCompleted();
            } else {
                listener.onTracingFailed();
            }
        }
    }

    public void clearCanvas() {
        drawPath.reset();
        tracingSuccessful = false;
        if (viewModel != null) {
            viewModel.clear();
        }
        invalidate();
    }

    public boolean isTracingSuccessful() {
        return tracingSuccessful;
    }
}
