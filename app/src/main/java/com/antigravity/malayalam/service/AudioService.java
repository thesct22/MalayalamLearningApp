package com.antigravity.malayalam.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class AudioService {
    private static final String TAG = "AudioService";
    
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private boolean ttsReady = false;
    private final Locale malayalamLocale = new Locale("ml", "IN");

    public AudioService() {
        // Default constructor for testing
    }

    public AudioService(Context context) {
        if (context != null) {
            tts = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(malayalamLocale);
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        ttsReady = true;
                    } else {
                        Log.e(TAG, "Malayalam TTS not supported");
                    }
                }
            });
            
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            }
        }
    }

    public boolean isReady() {
        return ttsReady;
    }
    
    public void speak(String text) {
        if (ttsReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ml_tts_id");
        }
    }
    
    public void startListening(RecognitionListener listener) {
        if (speechRecognizer != null) {
            speechRecognizer.setRecognitionListener(listener);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ml-IN");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            speechRecognizer.startListening(intent);
        }
    }
    
    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }
    
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
}
