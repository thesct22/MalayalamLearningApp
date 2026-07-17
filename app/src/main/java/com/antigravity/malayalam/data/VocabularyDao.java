package com.antigravity.malayalam.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Data Access Object for vocabulary operations.
 */
@Dao
public interface VocabularyDao {

    @Query("SELECT * FROM vocabulary_words")
    List<VocabularyWord> getAllWords();

    @Query("SELECT * FROM vocabulary_words WHERE category = :category")
    List<VocabularyWord> getWordsByCategory(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VocabularyWord> words);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VocabularyWord word);

    @Query("SELECT * FROM vocabulary_words WHERE id = :id")
    VocabularyWord getWordById(int id);

    @Query("SELECT * FROM vocabulary_words LEFT JOIN user_progress ON vocabulary_words.id = user_progress.word_id ORDER BY COALESCE(user_progress.mastery_level, 0.0) ASC LIMIT :limit")
    List<VocabularyWord> getWeakestWords(int limit);

    @Query("SELECT COUNT(*) FROM vocabulary_words")
    int getWordCount();

    @Query("DELETE FROM vocabulary_words")
    void deleteAll();
}
