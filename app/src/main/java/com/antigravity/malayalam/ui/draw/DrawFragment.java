package com.antigravity.malayalam.ui.draw;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.antigravity.malayalam.MainActivity;
import com.antigravity.malayalam.databinding.FragmentDrawBinding;
import com.antigravity.malayalam.service.AudioService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tracing practice Fragment. Connects with DrawingCanvasView.
 */
public class DrawFragment extends Fragment {

    public static class LetterData {
        public final String letter;
        public final String meaning;
        public final String phonetic;

        public LetterData(String letter, String meaning, String phonetic) {
            this.letter = letter;
            this.meaning = meaning;
            this.phonetic = phonetic;
        }
    }

    public static final List<LetterData> LETTERS = Arrays.asList(
            new LetterData("അ", "Vowel 'A'", "Pronounced like 'u' in cup"),
            new LetterData("ആ", "Vowel 'Aa'", "Pronounced like 'a' in father"),
            new LetterData("ഇ", "Vowel 'I'", "Pronounced like 'i' in hit"),
            new LetterData("ഈ", "Vowel 'Ee'", "Pronounced like 'ee' in see"),
            new LetterData("ഉ", "Vowel 'U'", "Pronounced like 'u' in put")
    );

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String ARG_LETTER = "arg_letter";
    private static final String ARG_MEANING = "arg_meaning";
    private static final String ARG_PHONETIC = "arg_phonetic";

    private FragmentDrawBinding binding;
    private DrawViewModel viewModel;
    private AudioService audioService;

    private int currentLetterIndex = 0;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startSpeechRecognition();
                } else {
                    Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            });

    public static DrawFragment newInstance(String letter, String meaning, String phonetic) {
        DrawFragment fragment = new DrawFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LETTER, letter);
        args.putString(ARG_MEANING, meaning);
        args.putString(ARG_PHONETIC, phonetic);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String initialLetter = getArguments().getString(ARG_LETTER, "അ");
            for (int i = 0; i < LETTERS.size(); i++) {
                if (LETTERS.get(i).letter.equals(initialLetter)) {
                    currentLetterIndex = i;
                    break;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDrawBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DrawViewModel.class);
        audioService = new AudioService(requireContext());

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        LetterData data = LETTERS.get(currentLetterIndex);
        binding.tvDrawLetter.setText(data.letter);
        binding.tvDrawMeaning.setText(data.meaning);
        binding.tvDrawPhonetic.setText(data.phonetic);
        binding.drawingCanvas.setLetter(data.letter);
    }

    private void setupListeners() {
        binding.drawingCanvas.setDrawingListener(new DrawingCanvasView.DrawingListener() {
            @Override
            public void onTracingProgress(int visitedPoints, int totalPoints) {
                binding.tvDrawSubtitle.setText("Traced " + visitedPoints + " of " + totalPoints + " guide nodes");
            }

            @Override
            public void onTracingCompleted() {
                showSuccessFeedback();
            }

            @Override
            public void onTracingFailed() {
                Toast.makeText(getContext(), "Incorrect stroke sequence", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnClearCanvas.setOnClickListener(v -> {
            binding.drawingCanvas.clearCanvas();
            binding.cardDrawFeedback.setVisibility(View.GONE);
            binding.tvDrawSubtitle.setText("Follow the numbered guide points in order");
        });

        binding.btnSubmitCanvas.setOnClickListener(v -> {
            if (binding.drawingCanvas.isTracingSuccessful()) {
                showSuccessFeedback();
            } else {
                Toast.makeText(getContext(), "Trace all guide points before submitting!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnDrawNext.setOnClickListener(v -> {
            if (currentLetterIndex < LETTERS.size() - 1) {
                currentLetterIndex++;
                setupUI();
                binding.drawingCanvas.clearCanvas();
                binding.cardDrawFeedback.setVisibility(View.GONE);
                binding.tvDrawSubtitle.setText("Follow the numbered guide points in order");
            } else {
                if (getActivity() instanceof MainActivity) {
                    getActivity().onBackPressed();
                }
            }
        });

        binding.btnSpeakLetter.setOnClickListener(v -> {
            if (audioService != null) {
                audioService.speak(LETTERS.get(currentLetterIndex).letter);
            }
        });

        binding.btnRecordSpeech.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                return;
            }
            startSpeechRecognition();
        });
    }

    private void startSpeechRecognition() {
        if (audioService == null || !audioService.isSpeechRecognitionAvailable()) {
            Toast.makeText(getContext(), "Speech recognition is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Listening...", Toast.LENGTH_SHORT).show();
        audioService.startListening(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error recognizing speech: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResults(Bundle results) {
                if (getContext() == null) return;
                
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0);
                    if (result.contains(LETTERS.get(currentLetterIndex).letter)) {
                        Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "You said: " + result, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void showSuccessFeedback() {
        binding.cardDrawFeedback.setVisibility(View.VISIBLE);
        viewModel.saveTracingResult(LETTERS.get(currentLetterIndex).letter, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (audioService != null) {
            audioService.shutdown();
            audioService = null;
        }
        binding = null;
    }
}
