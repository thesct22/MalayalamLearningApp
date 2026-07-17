package com.antigravity.malayalam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.antigravity.malayalam.data.LanguageTrack;
import com.antigravity.malayalam.data.QuizQuestion;
import com.antigravity.malayalam.data.VocabularyWord;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

/**
 * Local unit tests for quiz models and enum values.
 */
public class QuizStateUnitTest {

    @Test
    public void testLanguageTrackEnum() {
        assertEquals("en", LanguageTrack.ENGLISH_TO_MALAYALAM.getLanguageCode());
        assertEquals("zh", LanguageTrack.CHINESE_TO_MALAYALAM.getLanguageCode());
    }

    @Test
    public void testQuizQuestionModel() {
        VocabularyWord word = new VocabularyWord("നമസ്കാരം", "Namaskaram", "Hello", "你好", 1, "Greetings");
        List<String> options = Arrays.asList("Goodbye", "Hello", "Thank you", "Water");
        
        QuizQuestion question = new QuizQuestion(
                word,
                options,
                1,
                QuizQuestion.QuestionType.CLICK
        );

        assertNotNull(question.getWord());
        assertEquals("നമസ്കാരം", question.getWord().getMalayalamScript());
        assertEquals(4, question.getOptions().size());
        assertEquals("Hello", question.getOptions().get(1));
        assertEquals(1, question.getCorrectOptionIndex());
        assertEquals(QuizQuestion.QuestionType.CLICK, question.getType());
    }
}
