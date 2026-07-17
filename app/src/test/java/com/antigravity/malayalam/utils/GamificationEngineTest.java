package com.antigravity.malayalam.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class GamificationEngineTest {

    @Test
    public void testCalculateLevel() {
        GamificationEngine engine = new GamificationEngine();
        assertEquals(1, engine.calculateLevel(0));
        assertEquals(2, engine.calculateLevel(150));
    }
}
