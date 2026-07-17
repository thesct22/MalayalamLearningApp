package com.antigravity.malayalam.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.antigravity.malayalam.MainActivity;
import com.antigravity.malayalam.R;
import com.antigravity.malayalam.data.LanguageTrack;
import com.antigravity.malayalam.data.VocabularyWord;
import com.antigravity.malayalam.databinding.FragmentHomeBinding;
import com.antigravity.malayalam.utils.GamificationEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Fragment containing the landing page UI for learning track and stats.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private VocabularyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeViewModel();
        setupGamification();
    }

    private void setupGamification() {
        // Dummy data for task 1
        int dummyXp = 150;
        GamificationEngine engine = new GamificationEngine();
        int level = engine.calculateLevel(dummyXp);
        int streak = engine.calculateStreak(Collections.emptyList());

        binding.tvXp.setText(String.valueOf(dummyXp));
        binding.tvLevel.setText(String.valueOf(level));
        binding.tvStreak.setText(String.valueOf(streak));
        
        int progress = (dummyXp % 100);
        binding.progressLevel.setProgress(progress);
    }

    private void setupRecyclerView() {
        binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VocabularyAdapter();
        binding.rvVocabulary.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toggleGroupTrack.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_track_english) {
                    viewModel.setTrack(LanguageTrack.ENGLISH_TO_MALAYALAM);
                } else if (checkedId == R.id.btn_track_chinese) {
                    viewModel.setTrack(LanguageTrack.CHINESE_TO_MALAYALAM);
                }
            }
        });

        binding.btnStartQuiz.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToQuiz();
            }
        });

        binding.btnDrawPractice.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToDraw("അ", "Letter 'A'", "Pronounced like 'u' in cup");
            }
        });

        binding.btnRequestAiWords.setOnClickListener(v -> {
            viewModel.requestCustomAILearningTrack();
        });
    }

    private void observeViewModel() {
        viewModel.getVocabularyList().observe(getViewLifecycleOwner(), words -> {
            adapter.setWords(words, viewModel.getActiveTrack().getValue());
        });

        viewModel.getTotalWordsCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvWordsCount.setText(String.valueOf(count));
        });

        viewModel.getAverageMastery().observe(getViewLifecycleOwner(), mastery -> {
            binding.tvMasteryPercent.setText(mastery + "%");
            binding.progressMastery.setProgress(mastery);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadLocalData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> {
        private final List<VocabularyWord> words = new ArrayList<>();
        private LanguageTrack currentTrack = LanguageTrack.ENGLISH_TO_MALAYALAM;

        public void setWords(List<VocabularyWord> newWords, LanguageTrack track) {
            words.clear();
            if (newWords != null) {
                words.addAll(newWords);
            }
            currentTrack = track;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VocabularyWord word = words.get(position);
            holder.text1.setText(word.getMalayalamScript() + " (" + word.getPhonetic() + ")");
            String translation = currentTrack == LanguageTrack.ENGLISH_TO_MALAYALAM 
                    ? word.getEnglishTranslation() 
                    : word.getChineseTranslation();
            holder.text2.setText(translation + " - Category: " + word.getCategory());
        }

        @Override
        public int getItemCount() {
            return words.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text1;
            final TextView text2;

            ViewHolder(View view) {
                super(view);
                text1 = view.findViewById(android.R.id.text1);
                text2 = view.findViewById(android.R.id.text2);
            }
        }
    }
}
