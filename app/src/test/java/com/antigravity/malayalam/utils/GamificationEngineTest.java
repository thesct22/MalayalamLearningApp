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

    @Test
    public void testCalculateStreak() {
        GamificationEngine engine = new GamificationEngine();
        
        // Null or empty lists give 0 streak
        assertEquals(0, engine.calculateStreak(null));
        assertEquals(0, engine.calculateStreak(new ArrayList<>()));
        
        // Dummy implementation gives 1 if there is an item
        List<Date> dates = new ArrayList<>();
        dates.add(new Date());
        assertEquals(1, engine.calculateStreak(dates));
    }
}
