package com.antigravity.malayalam.ui.draw;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.antigravity.malayalam.MainActivity;
import com.antigravity.malayalam.databinding.FragmentDrawBinding;
import com.antigravity.malayalam.utils.TtsHelper;

/**
 * Tracing practice Fragment. Connects with DrawingCanvasView.
 */
public class DrawFragment extends Fragment {

    private static final String ARG_LETTER = "arg_letter";
    private static final String ARG_MEANING = "arg_meaning";
    private static final String ARG_PHONETIC = "arg_phonetic";

    private FragmentDrawBinding binding;
    private DrawViewModel viewModel;
    private TtsHelper ttsHelper;

    private String targetLetter = "അ";
    private String targetMeaning = "Letter 'A'";
    private String targetPhonetic = "A";

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
            targetLetter = getArguments().getString(ARG_LETTER, "അ");
            targetMeaning = getArguments().getString(ARG_MEANING, "Letter 'A'");
            targetPhonetic = getArguments().getString(ARG_PHONETIC, "A");
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
        ttsHelper = new TtsHelper(requireContext());

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.tvDrawLetter.setText(targetLetter);
        binding.tvDrawMeaning.setText(targetMeaning);
        binding.tvDrawPhonetic.setText(targetPhonetic);
        binding.drawingCanvas.setLetter(targetLetter);
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
            if (getActivity() instanceof MainActivity) {
                getActivity().onBackPressed();
            }
        });

        binding.btnSpeakLetter.setOnClickListener(v -> {
            if (ttsHelper != null) {
                ttsHelper.speak(targetLetter);
            }
        });
    }

    private void showSuccessFeedback() {
        binding.cardDrawFeedback.setVisibility(View.VISIBLE);
        viewModel.saveTracingResult(targetLetter, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ttsHelper != null) {
            ttsHelper.shutdown();
            ttsHelper = null;
        }
        binding = null;
    }
}
