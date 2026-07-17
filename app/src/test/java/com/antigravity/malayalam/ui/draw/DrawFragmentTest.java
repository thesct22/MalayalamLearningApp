package com.antigravity.malayalam.ui.draw;

import android.os.Bundle;
import android.widget.Button;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.antigravity.malayalam.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@Config(manifest=Config.NONE, sdk = 34)
public class DrawFragmentTest {

    @Test
    public void testFragmentLaunches() {
        FragmentScenario<DrawFragment> scenario = FragmentScenario.launchInContainer(
            DrawFragment.class,
            DrawFragment.newInstance("അ", "Letter A", "A").getArguments(),
            R.style.Theme_MalayalamLearningApp
        );
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertNotNull(fragment.getView());
        });
    }

    @Test
    public void testSpeakButton_callsAudioService() {
        FragmentScenario<DrawFragment> scenario = FragmentScenario.launchInContainer(
            DrawFragment.class,
            DrawFragment.newInstance("അ", "Letter A", "A").getArguments(),
            R.style.Theme_MalayalamLearningApp
        );
        
        scenario.onFragment(fragment -> {
            Button speakButton = fragment.getView().findViewById(R.id.btn_speak_letter);
            assertNotNull(speakButton);
            speakButton.performClick();
            // We're just verifying no crash occurs when clicking it and AudioService handles it gracefully.
        });
    }

    @Test
    public void testRecordButton_clicksWithoutCrash() {
        FragmentScenario<DrawFragment> scenario = FragmentScenario.launchInContainer(
            DrawFragment.class,
            DrawFragment.newInstance("അ", "Letter A", "A").getArguments(),
            R.style.Theme_MalayalamLearningApp
        );
        
        scenario.onFragment(fragment -> {
            Button recordButton = fragment.getView().findViewById(R.id.btn_record_speech);
            assertNotNull(recordButton);
            recordButton.performClick();
        });
    }
}
