package com.antigravity.malayalam.ui.draw;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.antigravity.malayalam.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@Config(manifest=Config.NONE, sdk = 34)
public class DrawFragmentTest {

    @Test
    public void testFragmentLaunches() {
        FragmentScenario<DrawFragment> scenario = FragmentScenario.launchInContainer(
            DrawFragment.class,
            DrawFragment.newInstance("അ", "Vowel 'A'", "Pronounced like 'u' in cup").getArguments(),
            R.style.Theme_MalayalamLearningApp
        );
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertNotNull(fragment.getView());
        });
    }
    
    @Test
    public void testNextButtonNavigatesLetters() {
        FragmentScenario<DrawFragment> scenario = FragmentScenario.launchInContainer(
            DrawFragment.class,
            DrawFragment.newInstance("അ", "Vowel 'A'", "Pronounced like 'u' in cup").getArguments(),
            R.style.Theme_MalayalamLearningApp
        );
        
        scenario.onFragment(fragment -> {
            TextView letterText = fragment.getView().findViewById(R.id.tv_draw_letter);
            Button nextButton = fragment.getView().findViewById(R.id.btn_draw_next);
            
            assertEquals("അ", letterText.getText().toString());
            
            nextButton.performClick(); // To Aa
            assertEquals("ആ", letterText.getText().toString());
            
            nextButton.performClick(); // To I
            assertEquals("ഇ", letterText.getText().toString());
            
            nextButton.performClick(); // To Ee
            assertEquals("ഈ", letterText.getText().toString());
            
            nextButton.performClick(); // To U
            assertEquals("ഉ", letterText.getText().toString());
            
            // Next click would call onBackPressed. Let's not test the Activity part here easily unless we mock.
        });
    }
}
