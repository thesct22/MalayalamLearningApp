package com.antigravity.malayalam;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.antigravity.malayalam.data.AppDatabase;
import com.antigravity.malayalam.data.VocabularyDao;
import com.antigravity.malayalam.data.VocabularyWord;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests running on an emulator or device.
 * Tests local Room transactions and UI navigation.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    private AppDatabase db;
    private VocabularyDao vocabularyDao;

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        vocabularyDao = db.vocabularyDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void testDatabaseInsertionAndQuery() {
        VocabularyWord word = new VocabularyWord("നമസ്കാരം", "Namaskaram", "Hello", "你好", 1, "Greetings");
        vocabularyDao.insert(word);
        List<VocabularyWord> all = vocabularyDao.getAllWords();
        assertEquals(1, all.size());
        assertEquals("നമസ്കാരം", all.get(0).getMalayalamScript());
    }

    @Test
    public void testMainActivityLaunchesAndHomeUIRenders() {
        onView(withId(R.id.tv_title)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_title)).check(matches(withText("Malayalam Academy")));
        onView(withId(R.id.card_stats)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToCanvasTracing() {
        onView(withId(R.id.btn_draw_practice)).perform(click());
        onView(withId(R.id.tv_draw_title)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_draw_title)).check(matches(withText("Character Tracing")));
        onView(withId(R.id.drawing_canvas)).check(matches(isDisplayed()));
    }
}
