package com.antigravity.malayalam.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity representing a Malayalam vocabulary word.
 */
@Entity(tableName = "vocabulary_words")
public class VocabularyWord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "malayalam_script")
    private String malayalamScript;

    @ColumnInfo(name = "phonetic")
    private String phonetic;

    @ColumnInfo(name = "english_translation")
    private String englishTranslation;

    @ColumnInfo(name = "chinese_translation")
    private String chineseTranslation;

    @ColumnInfo(name = "difficulty_level")
    private int difficultyLevel;

    @ColumnInfo(name = "category")
    private String category;

    public VocabularyWord(String malayalamScript, String phonetic, String englishTranslation, 
                          String chineseTranslation, int difficultyLevel, String category) {
        this.malayalamScript = malayalamScript;
        this.phonetic = phonetic;
        this.englishTranslation = englishTranslation;
        this.chineseTranslation = chineseTranslation;
        this.difficultyLevel = difficultyLevel;
        this.category = category;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMalayalamScript() { return malayalamScript; }
    public void setMalayalamScript(String malayalamScript) { this.malayalamScript = malayalamScript; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getEnglishTranslation() { return englishTranslation; }
    public void setEnglishTranslation(String englishTranslation) { this.englishTranslation = englishTranslation; }

    public String getChineseTranslation() { return chineseTranslation; }
    public void setChineseTranslation(String chineseTranslation) { this.chineseTranslation = chineseTranslation; }

    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
