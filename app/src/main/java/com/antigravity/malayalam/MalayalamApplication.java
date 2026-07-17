package com.antigravity.malayalam;

import android.app.Application;

import com.antigravity.malayalam.ui.draw.LetterTemplates;

public class MalayalamApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LetterTemplates.initialize(
                this,
                R.font.noto_sans_malayalam_regular,
                false
        );
    }
}
