package com.antigravity.malayalam.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

/**
 * TextToSpeech wrapper configured for Malayalam voice output.
 */
public class TtsHelper implements TextToSpeech.OnInitListener {

    private static final String TAG = "TtsHelper";
    private TextToSpeech tts;
    private boolean isReady = false;
    private final Locale malayalamLocale = new Locale("ml", "IN");

    public TtsHelper(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(malayalamLocale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Malayalam language not supported or missing data on this device");
                isReady = false;
            } else {
                isReady = true;
            }
        } else {
            Log.e(TAG, "TextToSpeech initialization failed");
            isReady = false;
        }
    }

    public void speak(String text) {
        if (isReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ml_tts_id");
        } else {
            Log.w(TAG, "TTS not ready or initialized yet");
        }
    }

    public boolean isReady() {
        return isReady;
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
