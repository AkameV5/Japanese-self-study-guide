package com.example.japanese_self_study_guide.dictionary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;

import java.util.List;

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.WordViewHolder> {

    private List<Word> words;

    public DictionaryAdapter(List<Word> words) {
        this.words = words;
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView textWord, textReading, textTranslation, textCategory;

        public WordViewHolder(View itemView) {
            super(itemView);
            textWord = itemView.findViewById(R.id.textWord);
            textReading = itemView.findViewById(R.id.textReading);
            textTranslation = itemView.findViewById(R.id.textTranslation);
            textCategory = itemView.findViewById(R.id.textCategory);
        }
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dictionary, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WordViewHolder holder, int position) {
        Word w = words.get(position);
        holder.textWord.setText(w.getWord());
        holder.textReading.setText(w.getReading());
        holder.textTranslation.setText(w.getTranslation());
        holder.textCategory.setText(w.getCategory());
    }

    @Override
    public int getItemCount() {
        return words.size();
    }
}
