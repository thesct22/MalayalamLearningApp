package com.antigravity.malayalam;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.antigravity.malayalam.ui.home.HomeFragment;
import com.antigravity.malayalam.ui.quiz.QuizFragment;
import com.antigravity.malayalam.ui.draw.DrawFragment;

/**
 * Main Activity hosting the fragment container and managing transitions.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    public void navigateToHome() {
        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void navigateToQuiz() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new QuizFragment())
                .addToBackStack(null)
                .commit();
    }

    public void navigateToDraw(String letter, String meaning, String phonetic, boolean isSequential) {
        DrawFragment fragment = DrawFragment.newInstance(letter, meaning, phonetic, isSequential);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
