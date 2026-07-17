package com.antigravity.malayalam.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity tracking student performance for each word.
 */
@Entity(tableName = "user_progress")
public class UserProgress {

    @PrimaryKey
    @ColumnInfo(name = "word_id")
    private int wordId;

    @ColumnInfo(name = "correct_count")
    private int correctCount;

    @ColumnInfo(name = "incorrect_count")
    private int incorrectCount;

    @ColumnInfo(name = "last_practiced_time")
    private long lastPracticedTime;

    @ColumnInfo(name = "mastery_level")
    private float masteryLevel;

    public UserProgress(int wordId, int correctCount, int incorrectCount, long lastPracticedTime, float masteryLevel) {
        this.wordId = wordId;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.lastPracticedTime = lastPracticedTime;
        this.masteryLevel = masteryLevel;
    }

    public int getWordId() { return wordId; }
    public void setWordId(int wordId) { this.wordId = wordId; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getIncorrectCount() { return incorrectCount; }
    public void setIncorrectCount(int incorrectCount) { this.incorrectCount = incorrectCount; }

    public long getLastPracticedTime() { return lastPracticedTime; }
    public void setLastPracticedTime(long lastPracticedTime) { this.lastPracticedTime = lastPracticedTime; }

    public float getMasteryLevel() { return masteryLevel; }
    public void setMasteryLevel(float masteryLevel) { this.masteryLevel = masteryLevel; }
}
