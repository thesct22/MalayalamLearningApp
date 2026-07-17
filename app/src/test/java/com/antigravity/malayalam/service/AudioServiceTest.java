package com.antigravity.malayalam.service;

import org.junit.Test;
import static org.junit.Assert.assertFalse;

public class AudioServiceTest {
    @Test
    public void testAudioServiceInitState() {
        AudioService service = new AudioService();
        assertFalse(service.isReady());
    }
}
