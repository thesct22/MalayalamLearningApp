package com.antigravity.malayalam.ui.home;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.antigravity.malayalam.data.DataRepository;
import com.antigravity.malayalam.data.LanguageTrack;
import com.antigravity.malayalam.data.VocabularyWord;
import com.antigravity.malayalam.service.GeminiService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Home screen managing tracks and progress stats.
 */
public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";
    private final DataRepository repository;
    private final GeminiService geminiService;

    private final MutableLiveData<List<VocabularyWord>> vocabularyList = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalWordsCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> averageMastery = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<LanguageTrack> activeTrack = new MutableLiveData<>(LanguageTrack.ENGLISH_TO_MALAYALAM);

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.repository = new DataRepository(application);
        this.geminiService = GeminiService.getInstance();
        loadLocalData();
    }

    public LiveData<List<VocabularyWord>> getVocabularyList() { return vocabularyList; }
    public LiveData<Integer> getTotalWordsCount() { return totalWordsCount; }
    public LiveData<Integer> getAverageMastery() { return averageMastery; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<LanguageTrack> getActiveTrack() { return activeTrack; }

    public void setTrack(LanguageTrack track) {
        activeTrack.setValue(track);
        loadLocalData();
    }

    public void loadLocalData() {
        isLoading.setValue(true);
        repository.getAllWords(words -> {
            vocabularyList.setValue(words);
            totalWordsCount.setValue(words.size());
            isLoading.setValue(false);
        });

        repository.getAverageMastery(mastery -> {
            averageMastery.setValue((int) (mastery * 100));
        });
    }

    public void requestCustomAILearningTrack() {
        isLoading.setValue(true);
        geminiService.requestCustomLearningTrack(activeTrack.getValue(), new GeminiService.GeminiCallback<String>() {
            @Override
            public void onSuccess(String jsonResult) {
                parseAndInsertAITrack(jsonResult);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Failed to load custom AI track", t);
                isLoading.setValue(false);
            }
        });
    }

    private void parseAndInsertAITrack(String json) {
        try {
            JSONArray array = new JSONArray(json);
            List<VocabularyWord> words = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                words.add(new VocabularyWord(
                        obj.getString("malayalamScript"),
                        obj.getString("phonetic"),
                        obj.getString("englishTranslation"),
                        obj.getString("chineseTranslation"),
                        obj.getInt("difficultyLevel"),
                        obj.getString("category")
                ));
            }
            repository.insertWords(words, () -> {
                loadLocalData();
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error for learning track response", e);
            isLoading.setValue(false);
        }
    }
}
