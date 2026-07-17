package com.antigravity.malayalam.data;

import java.util.List;

/**
 * Model class representing a single quiz question.
 */
public class QuizQuestion {

    public enum QuestionType {
        CLICK,   // Multiple choice text matching
        LISTEN,  // Audio text matching
        SPEAK,   // Pronunciation test
        DRAW     // Character tracing practice
    }

    private final VocabularyWord word;
    private final List<String> options;
    private final int correctOptionIndex;
    private final QuestionType type;

    public QuizQuestion(VocabularyWord word, List<String> options, int correctOptionIndex, QuestionType type) {
        this.word = word;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.type = type;
    }

    public VocabularyWord getWord() { return word; }
    public List<String> getOptions() { return options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public QuestionType getType() { return type; }
}
