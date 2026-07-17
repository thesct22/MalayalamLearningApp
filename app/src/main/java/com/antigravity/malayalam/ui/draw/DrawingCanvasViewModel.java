package com.antigravity.malayalam.ui.draw;

import android.graphics.PointF;
import java.util.ArrayList;
import java.util.List;

public class DrawingCanvasViewModel {
    private List<PointF> points = new ArrayList<>();
    public void addPoint(float x, float y) { points.add(new PointF(x, y)); }
    public List<PointF> getPoints() { return points; }
    public void clear() { points.clear(); }
    
    public boolean isDrawn(int minPoints) {
        return points.size() >= minPoints;
    }
}
