package com.antigravity.malayalam.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class handling database transactions and coordination.
 */
public class DataRepository {

    private final VocabularyDao vocabularyDao;
    private final UserProgressDao userProgressDao;
    private final ExecutorService executorService;
    private final Handler mainThreadHandler;

    public interface RepositoryCallback<T> {
        void onComplete(T result);
    }

    public DataRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.vocabularyDao = db.vocabularyDao();
        this.userProgressDao = db.userProgressDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
        
        // Seed database if empty
        executorService.execute(this::seedDatabaseIfNeeded);
    }

    public void getAllWords(RepositoryCallback<List<VocabularyWord>> callback) {
        executorService.execute(() -> {
            List<VocabularyWord> words = vocabularyDao.getAllWords();
            mainThreadHandler.post(() -> callback.onComplete(words));
        });
    }

    public void getWordsByCategory(String category, RepositoryCallback<List<VocabularyWord>> callback) {
        executorService.execute(() -> {
            List<VocabularyWord> words = vocabularyDao.getWordsByCategory(category);
            mainThreadHandler.post(() -> callback.onComplete(words));
        });
    }

    public void getWeakestWords(int limit, RepositoryCallback<List<VocabularyWord>> callback) {
        executorService.execute(() -> {
            List<VocabularyWord> words = vocabularyDao.getWeakestWords(limit);
            mainThreadHandler.post(() -> callback.onComplete(words));
        });
    }

    public void insertWords(List<VocabularyWord> words, Runnable onComplete) {
        executorService.execute(() -> {
            vocabularyDao.insertAll(words);
            if (onComplete != null) {
                mainThreadHandler.post(onComplete);
            }
        });
    }

    public void getProgressForWord(int wordId, RepositoryCallback<UserProgress> callback) {
        executorService.execute(() -> {
            UserProgress progress = userProgressDao.getProgressForWord(wordId);
            mainThreadHandler.post(() -> callback.onComplete(progress));
        });
    }

    public void recordAnswer(int wordId, boolean isCorrect, Runnable onComplete) {
        executorService.execute(() -> {
            UserProgress progress = userProgressDao.getProgressForWord(wordId);
            if (progress == null) {
                progress = new UserProgress(wordId, 0, 0, 0, 0f);
            }
            if (isCorrect) {
                progress.setCorrectCount(progress.getCorrectCount() + 1);
            } else {
                progress.setIncorrectCount(progress.getIncorrectCount() + 1);
            }
            progress.setLastPracticedTime(System.currentTimeMillis());

            int total = progress.getCorrectCount() + progress.getIncorrectCount();
            float mastery = (float) progress.getCorrectCount() / total;
            if (total < 3) {
                mastery = mastery * 0.5f; // Penalize low attempt count
            }
            progress.setMasteryLevel(mastery);

            userProgressDao.saveProgress(progress);
            if (onComplete != null) {
                mainThreadHandler.post(onComplete);
            }
        });
    }

    public void getAverageMastery(RepositoryCallback<Double> callback) {
        executorService.execute(() -> {
            Double mastery = userProgressDao.getAverageMastery();
            mainThreadHandler.post(() -> callback.onComplete(mastery != null ? mastery : 0.0));
        });
    }

    public void getMasteredWordCount(RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            int count = userProgressDao.getMasteredWordCount();
            mainThreadHandler.post(() -> callback.onComplete(count));
        });
    }

    private void seedDatabaseIfNeeded() {
        if (vocabularyDao.getWordCount() == 0) {
            List<VocabularyWord> seedList = new ArrayList<>();
            seedList.add(new VocabularyWord("നമസ്കാരം", "Namaskaram", "Hello", "你好", 1, "Greetings"));
            seedList.add(new VocabularyWord("നന്ദി", "Nandi", "Thank you", "谢谢", 1, "Greetings"));
            seedList.add(new VocabularyWord("അതെ", "Athe", "Yes", "是的", 1, "Conversational"));
            seedList.add(new VocabularyWord("അല്ല", "Alla", "No", "不是", 1, "Conversational"));
            seedList.add(new VocabularyWord("വെള്ളം", "Vellam", "Water", "水", 2, "Essentials"));
            seedList.add(new VocabularyWord("അമ്മ", "Amma", "Mother", "母亲", 1, "Family"));
            seedList.add(new VocabularyWord("അച്ഛൻ", "Achhan", "Father", "父亲", 1, "Family"));
            seedList.add(new VocabularyWord("പേര്", "Peru", "Name", "名字", 1, "Conversational"));
            seedList.add(new VocabularyWord("വീട്", "Veedu", "House", "房子", 2, "Essentials"));
            seedList.add(new VocabularyWord("കൂട്ടുകാരൻ", "Koottukaran", "Friend (Male)", "朋友 (男)", 2, "Social"));
            seedList.add(new VocabularyWord("സുഖമാണോ?", "Sukhamano?", "How are you?", "你好吗？", 2, "Greetings"));
            seedList.add(new VocabularyWord("ഭക്ഷണം", "Bhakshanam", "Food", "食物", 2, "Essentials"));
            vocabularyDao.insertAll(seedList);
        }
    }
}
