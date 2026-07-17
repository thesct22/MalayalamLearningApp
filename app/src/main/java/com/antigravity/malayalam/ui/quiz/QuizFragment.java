package com.antigravity.malayalam.ui.quiz;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.antigravity.malayalam.MainActivity;
import com.antigravity.malayalam.R;
import com.antigravity.malayalam.data.QuizQuestion;
import com.antigravity.malayalam.databinding.FragmentQuizBinding;
import com.antigravity.malayalam.utils.AudioRecordHelper;
import com.antigravity.malayalam.utils.TtsHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Quiz screen displaying multiple choice, listening, and speaking modules dynamically.
 */
public class QuizFragment extends Fragment {

    private static final int MIC_PERMISSION_REQUEST_CODE = 200;

    private FragmentQuizBinding binding;
    private QuizViewModel viewModel;
    private TtsHelper ttsHelper;
    private AudioRecordHelper audioRecordHelper;
    private OptionsAdapter optionsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQuizBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QuizViewModel.class);

        ttsHelper = new TtsHelper(requireContext());
        audioRecordHelper = new AudioRecordHelper();

        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        binding.rvOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        optionsAdapter = new OptionsAdapter(position -> {
            if (Boolean.FALSE.equals(viewModel.getShowFeedback().getValue())) {
                viewModel.submitAnswer(position);
            }
        });
        binding.rvOptions.setAdapter(optionsAdapter);
    }

    private void setupListeners() {
        binding.btnCloseQuiz.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                getActivity().onBackPressed();
            }
        });

        binding.btnPlayAudio.setOnClickListener(v -> {
            QuizQuestion q = viewModel.getCurrentQuestion().getValue();
            if (q != null && ttsHelper != null) {
                ttsHelper.speak(q.getWord().getMalayalamScript());
            }
        });

        binding.fabRecord.setOnClickListener(v -> {
            if (checkMicrophonePermission()) {
                toggleRecording();
            } else {
                requestMicrophonePermission();
            }
        });

        binding.btnNextQuestion.setOnClickListener(v -> {
            viewModel.nextQuestion();
        });
    }

    private void toggleRecording() {
        if (audioRecordHelper.isRecording()) {
            audioRecordHelper.stopRecording();
            binding.fabRecord.setImageResource(android.R.drawable.presence_audio_online);
            binding.tvSpeakHint.setText("Recording complete. Evaluating...");
            viewModel.evaluatePronunciation();
        } else {
            String filePath = requireContext().getCacheDir().getAbsolutePath() + "/audio_temp.mp4";
            audioRecordHelper.startRecording(filePath);
            binding.fabRecord.setImageResource(android.R.drawable.ic_media_pause);
            binding.tvSpeakHint.setText("Listening... tap again to stop.");
        }
    }

    private void observeViewModel() {
        viewModel.getCurrentQuestionIndex().observe(getViewLifecycleOwner(), index -> {
            if (index != null && index >= 0) {
                binding.tvQuizProgress.setText("Question " + (index + 1) + " of 5");
            }
        });

        viewModel.getQuizProgress().observe(getViewLifecycleOwner(), progress -> {
            binding.pbQuiz.setProgress(progress);
        });

        viewModel.getCurrentQuestion().observe(getViewLifecycleOwner(), this::displayQuestion);

        viewModel.getShowFeedback().observe(getViewLifecycleOwner(), show -> {
            binding.cardFeedback.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsCorrectAnswer().observe(getViewLifecycleOwner(), correct -> {
            if (Boolean.TRUE.equals(correct)) {
                binding.layoutFeedbackBg.setBackgroundColor(Color.parseColor("#E8F5E9"));
                binding.tvFeedbackTitle.setText("Correct Answer!");
                binding.tvFeedbackTitle.setTextColor(Color.parseColor("#2E7D32"));
                binding.btnNextQuestion.setBackgroundColor(Color.parseColor("#2E7D32"));
            } else {
                binding.layoutFeedbackBg.setBackgroundColor(Color.parseColor("#FFEBEE"));
                binding.tvFeedbackTitle.setText("Incorrect");
                binding.tvFeedbackTitle.setTextColor(Color.parseColor("#C62828"));
                binding.btnNextQuestion.setBackgroundColor(Color.parseColor("#C62828"));
            }
        });

        viewModel.getFeedbackDetail().observe(getViewLifecycleOwner(), detail -> {
            binding.tvFeedbackDetail.setText(detail);
        });

        viewModel.getIsQuizFinished().observe(getViewLifecycleOwner(), finished -> {
            if (Boolean.TRUE.equals(finished)) {
                Toast.makeText(getContext(), "Quiz Completed!", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToHome();
                }
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            // Can show loading indicator if needed
        });
    }

    private void displayQuestion(QuizQuestion question) {
        if (question == null) return;

        binding.tvQuestionText.setVisibility(View.GONE);
        binding.tvTargetWord.setVisibility(View.GONE);
        binding.tvTargetWordPhonetic.setVisibility(View.GONE);
        binding.btnPlayAudio.setVisibility(View.GONE);
        binding.layoutSpeakControls.setVisibility(View.GONE);
        binding.rvOptions.setVisibility(View.GONE);

        switch (question.getType()) {
            case CLICK:
                binding.tvQuestionText.setVisibility(View.VISIBLE);
                binding.tvQuestionText.setText("Match the correct translation:");
                binding.tvTargetWord.setVisibility(View.VISIBLE);
                binding.tvTargetWord.setText(question.getWord().getMalayalamScript());
                binding.tvTargetWordPhonetic.setVisibility(View.VISIBLE);
                binding.tvTargetWordPhonetic.setText(question.getWord().getPhonetic());
                binding.rvOptions.setVisibility(View.VISIBLE);
                optionsAdapter.setOptions(question.getOptions());
                break;
            case LISTEN:
                binding.tvQuestionText.setVisibility(View.VISIBLE);
                binding.tvQuestionText.setText("Listen and match translation:");
                binding.btnPlayAudio.setVisibility(View.VISIBLE);
                binding.rvOptions.setVisibility(View.VISIBLE);
                optionsAdapter.setOptions(question.getOptions());
                if (ttsHelper != null) {
                    ttsHelper.speak(question.getWord().getMalayalamScript());
                }
                break;
            case SPEAK:
                binding.tvQuestionText.setVisibility(View.VISIBLE);
                binding.tvQuestionText.setText("Speak this word aloud:");
                binding.tvTargetWord.setVisibility(View.VISIBLE);
                binding.tvTargetWord.setText(question.getWord().getMalayalamScript());
                binding.tvTargetWordPhonetic.setVisibility(View.VISIBLE);
                binding.tvTargetWordPhonetic.setText(question.getWord().getPhonetic());
                binding.layoutSpeakControls.setVisibility(View.VISIBLE);
                binding.tvSpeakHint.setText("Tap mic and speak now");
                binding.fabRecord.setImageResource(android.R.drawable.presence_audio_online);
                break;
            case DRAW:
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToDraw(
                            question.getWord().getMalayalamScript(),
                            "Trace character " + question.getWord().getMalayalamScript(),
                            question.getWord().getPhonetic(),
                            false
                    );
                }
                break;
        }
    }

    private boolean checkMicrophonePermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestMicrophonePermission() {
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MIC_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleRecording();
            } else {
                Toast.makeText(getContext(), "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ttsHelper != null) {
            ttsHelper.shutdown();
            ttsHelper = null;
        }
        if (audioRecordHelper != null) {
            audioRecordHelper.stopRecording();
            audioRecordHelper = null;
        }
        binding = null;
    }

    private static class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {
        private final List<String> options = new ArrayList<>();
        private final OnOptionClickListener listener;

        interface OnOptionClickListener {
            void onOptionClick(int position);
        }

        public OptionsAdapter(OnOptionClickListener listener) {
            this.listener = listener;
        }

        public void setOptions(List<String> list) {
            options.clear();
            if (list != null) {
                options.addAll(list);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_option, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.text.setText(options.get(position));
            holder.itemView.setOnClickListener(v -> listener.onOptionClick(position));
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text;
            ViewHolder(View view) {
                super(view);
                text = view.findViewById(R.id.tv_option_text);
            }
        }
    }
}
