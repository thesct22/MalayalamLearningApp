package com.antigravity.malayalam.ui.draw;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DrawingCanvasViewModelTest {
    @Test
    public void testCanvasClearState() {
        DrawingCanvasViewModel vm = new DrawingCanvasViewModel();
        vm.addPoint(10f, 10f);
        assertFalse(vm.getPoints().isEmpty());
        vm.clear();
        assertTrue(vm.getPoints().isEmpty());
    }

    @Test
    public void testIsDrawn() {
        DrawingCanvasViewModel vm = new DrawingCanvasViewModel();
        
        // Initial state
        assertFalse("Should be false initially", vm.isDrawn(10));
        
        // Add 5 points (less than minPoints=10)
        for (int i = 0; i < 5; i++) {
            vm.addPoint(i, i);
        }
        assertFalse("Should be false with less than minPoints", vm.isDrawn(10));
        
        // Add 5 more points (equals minPoints=10)
        for (int i = 5; i < 10; i++) {
            vm.addPoint(i, i);
        }
        assertTrue("Should be true with minPoints or more", vm.isDrawn(10));
    }
}
