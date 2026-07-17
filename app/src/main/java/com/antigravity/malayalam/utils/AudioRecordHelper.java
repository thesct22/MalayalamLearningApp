package com.antigravity.malayalam.utils;

import android.media.MediaRecorder;
import android.util.Log;
import java.io.IOException;

/**
 * Helper class for recording audio using MediaRecorder.
 */
public class AudioRecordHelper {

    private static final String TAG = "AudioRecordHelper";
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    public void startRecording(String outputFilePath) {
        if (isRecording) {
            stopRecording();
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(outputFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Audio recording started, saving to " + outputFilePath);
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed for MediaRecorder", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "start() failed for MediaRecorder", e);
        }
    }

    public void stopRecording() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                Log.d(TAG, "Audio recording stopped");
            } catch (RuntimeException stopException) {
                // handle case where stop is called immediately after start
                Log.e(TAG, "stop() failed or no audio captured", stopException);
            } finally {
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
            }
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}
