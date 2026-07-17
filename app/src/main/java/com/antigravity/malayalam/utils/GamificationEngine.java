package com.antigravity.malayalam.utils;

import java.util.Date;
import java.util.List;

public class GamificationEngine {
    public int calculateLevel(int xp) {
        return (xp / 100) + 1;
    }

    public int calculateStreak(List<Date> dates) {
        // Dummy implementation for now to satisfy interface requirement mentioned in brief
        return dates == null || dates.isEmpty() ? 0 : 1;
    }
}
