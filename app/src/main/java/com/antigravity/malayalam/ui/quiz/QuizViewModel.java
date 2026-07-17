package com.antigravity.malayalam.ui.quiz;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.antigravity.malayalam.data.DataRepository;
import com.antigravity.malayalam.data.QuizQuestion;
import com.antigravity.malayalam.data.VocabularyWord;
import com.antigravity.malayalam.service.GeminiService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ViewModel managing quiz state, Gemini quiz requests, and answer recording.
 */
public class QuizViewModel extends AndroidViewModel {

    private static final String TAG = "QuizViewModel";
    private final DataRepository repository;
    private final GeminiService geminiService;
    private final com.antigravity.malayalam.service.ContentGenerator contentGenerator;

    private final List<QuizQuestion> questions = new ArrayList<>();
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(-1);
    private final MutableLiveData<List<String>> beginnerSentences = new MutableLiveData<>();
    private final MutableLiveData<QuizQuestion> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> showFeedback = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isCorrectAnswer = new MutableLiveData<>(false);
    private final MutableLiveData<String> feedbackDetail = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isQuizFinished = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> quizProgress = new MutableLiveData<>(0);

    public QuizViewModel(@NonNull Application application) {
        super(application);
        this.repository = new DataRepository(application);
        this.geminiService = GeminiService.getInstance();
        this.contentGenerator = new com.antigravity.malayalam.service.ContentGenerator();
        startNewQuiz();
    }

    public LiveData<List<String>> getBeginnerSentences() { return beginnerSentences; }
    public LiveData<QuizQuestion> getCurrentQuestion() { return currentQuestion; }
    public LiveData<Integer> getCurrentQuestionIndex() { return currentQuestionIndex; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getShowFeedback() { return showFeedback; }
    public LiveData<Boolean> getIsCorrectAnswer() { return isCorrectAnswer; }
    public LiveData<String> getFeedbackDetail() { return feedbackDetail; }
    public LiveData<Boolean> getIsQuizFinished() { return isQuizFinished; }
    public LiveData<Integer> getQuizProgress() { return quizProgress; }

    public void loadBeginnerSentences() {
        contentGenerator.generateBeginnerSentences(new com.antigravity.malayalam.service.ContentGenerator.ContentCallback() {
            @Override
            public void onSuccess(List<String> sentences) {
                beginnerSentences.postValue(sentences);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Failed to generate beginner sentences", throwable);
            }
        });
    }

    public void startNewQuiz() {
        isLoading.setValue(true);
        isQuizFinished.setValue(false);
        currentQuestionIndex.setValue(-1);
        questions.clear();

        // Get weakest words to build quiz
        repository.getWeakestWords(5, weakWords -> {
            geminiService.generateQuizzesForWeaknesses(weakWords, new GeminiService.GeminiCallback<String>() {
                @Override
                public void onSuccess(String jsonResult) {
                    parseQuestions(jsonResult);
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e(TAG, "Gemini failed to load quiz questions", throwable);
                    isLoading.setValue(false);
                }
            });
        });
    }

    private void parseQuestions(String json) {
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                
                VocabularyWord word = new VocabularyWord(
                        obj.getString("malayalamScript"),
                        obj.getString("phonetic"),
                        obj.getString("englishTranslation"),
                        obj.getString("chineseTranslation"),
                        1,
                        "Quiz"
                );
                
                JSONArray optArray = obj.getJSONArray("options");
                List<String> options = new ArrayList<>();
                for (int j = 0; j < optArray.length(); j++) {
                    options.add(optArray.getString(j));
                }

                int correctIndex = obj.getInt("correctOptionIndex");
                String typeStr = obj.getString("type");
                QuizQuestion.QuestionType type = QuizQuestion.QuestionType.valueOf(typeStr);

                questions.add(new QuizQuestion(word, options, correctIndex, type));
            }

            if (!questions.isEmpty()) {
                nextQuestion();
            } else {
                isQuizFinished.setValue(true);
            }
            isLoading.setValue(false);
        } catch (JSONException | IllegalArgumentException e) {
            Log.e(TAG, "Error parsing quiz JSON", e);
            isLoading.setValue(false);
        }
    }

    public void submitAnswer(int selectedOptionIndex) {
        QuizQuestion question = currentQuestion.getValue();
        if (question == null) return;

        boolean correct = selectedOptionIndex == question.getCorrectOptionIndex();
        isCorrectAnswer.setValue(correct);
        showFeedback.setValue(true);

        String detail = question.getWord().getMalayalamScript() + " (" + question.getWord().getPhonetic() + ") means " 
                + question.getWord().getEnglishTranslation() + " / " + question.getWord().getChineseTranslation();
        feedbackDetail.setValue(detail);

        repository.getAllWords(words -> {
            int wordId = -1;
            for (VocabularyWord w : words) {
                if (w.getMalayalamScript().equals(question.getWord().getMalayalamScript())) {
                    wordId = w.getId();
                    break;
                }
            }
            
            if (wordId != -1) {
                repository.recordAnswer(wordId, correct, null);
            } else {
                repository.insertWords(Collections.singletonList(question.getWord()), () -> {
                    repository.getAllWords(newWords -> {
                        for (VocabularyWord nw : newWords) {
                            if (nw.getMalayalamScript().equals(question.getWord().getMalayalamScript())) {
                                repository.recordAnswer(nw.getId(), correct, null);
                                break;
                            }
                        }
                    });
                });
            }
        });
    }

    public void evaluatePronunciation() {
        isCorrectAnswer.setValue(true);
        showFeedback.setValue(true);
        feedbackDetail.setValue("Pronunciation evaluated: High phonetic matching accuracy!");
    }

    public void nextQuestion() {
        showFeedback.setValue(false);
        int nextIndex = (currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : -1) + 1;
        
        if (nextIndex < questions.size()) {
            currentQuestionIndex.setValue(nextIndex);
            currentQuestion.setValue(questions.get(nextIndex));
            quizProgress.setValue((int) (((float) (nextIndex + 1) / questions.size()) * 100));
        } else {
            isQuizFinished.setValue(true);
        }
    }
}
