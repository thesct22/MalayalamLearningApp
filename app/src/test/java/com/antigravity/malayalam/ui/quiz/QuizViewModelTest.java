package com.antigravity.malayalam.ui.quiz;

import android.app.Application;

import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;

import com.antigravity.malayalam.service.ContentGenerator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class QuizViewModelTest {

    private QuizViewModel viewModel;
    private Application application;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        viewModel = new QuizViewModel(application);
    }

    @Test
    public void testLoadBeginnerSentences() throws InterruptedException {
        // Create a mock ContentGenerator that immediately returns results
        ContentGenerator mockGenerator = new ContentGenerator() {
            @Override
            public void generateBeginnerSentences(ContentCallback callback) {
                callback.onSuccess(Arrays.asList("Test1 - English1", "Test2 - English2"));
            }
        };
        viewModel.setContentGenerator(mockGenerator);

        final List<String>[] loadedSentences = new List[1];

        // Observe the LiveData
        Observer<List<String>> observer = sentences -> {
            if (sentences != null) {
                loadedSentences[0] = sentences;
            }
        };
        
        viewModel.getBeginnerSentences().observeForever(observer);
        
        viewModel.loadBeginnerSentences();
        
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        assertNotNull(loadedSentences[0]);
        assertEquals(2, loadedSentences[0].size());
        assertEquals("Test1 - English1", loadedSentences[0].get(0));
        
        viewModel.getBeginnerSentences().removeObserver(observer);
    }
}
