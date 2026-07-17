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
}
