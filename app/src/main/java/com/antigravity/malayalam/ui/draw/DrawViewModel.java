package com.antigravity.malayalam.ui.draw;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.antigravity.malayalam.data.DataRepository;
import com.antigravity.malayalam.data.VocabularyWord;
import java.util.Collections;

/**
 * ViewModel for character tracing screen.
 */
public class DrawViewModel extends AndroidViewModel {

    private final DataRepository repository;

    public DrawViewModel(@NonNull Application application) {
        super(application);
        this.repository = new DataRepository(application);
    }

    public void saveTracingResult(String letter, boolean correct) {
        repository.getAllWords(words -> {
            int wordId = -1;
            for (VocabularyWord w : words) {
                if (w.getMalayalamScript().equals(letter)) {
                    wordId = w.getId();
                    break;
                }
            }

            if (wordId != -1) {
                repository.recordAnswer(wordId, correct, null);
            } else {
                VocabularyWord newLetter = new VocabularyWord(letter, letter, "Letter " + letter, "字母 " + letter, 1, "Alphabet");
                repository.insertWords(Collections.singletonList(newLetter), () -> {
                    repository.getAllWords(newWords -> {
                        for (VocabularyWord nw : newWords) {
                            if (nw.getMalayalamScript().equals(letter)) {
                                repository.recordAnswer(nw.getId(), correct, null);
                                break;
                            }
                        }
                    });
                });
            }
        });
    }
}
