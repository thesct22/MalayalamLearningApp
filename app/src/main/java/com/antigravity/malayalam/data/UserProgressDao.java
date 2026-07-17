package com.antigravity.malayalam.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Data Access Object for tracking progress.
 */
@Dao
public interface UserProgressDao {

    @Query("SELECT * FROM user_progress WHERE word_id = :wordId")
    UserProgress getProgressForWord(int wordId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveProgress(UserProgress progress);

    @Query("SELECT AVG(mastery_level) FROM user_progress")
    Double getAverageMastery();

    @Query("SELECT COUNT(*) FROM user_progress WHERE mastery_level >= 0.8")
    int getMasteredWordCount();

    @Query("DELETE FROM user_progress")
    void deleteAll();
}
