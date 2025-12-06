package com.example.japanese_self_study_guide.texts_and_translation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;

import java.util.List;

public class TextsAdapter extends RecyclerView.Adapter<TextsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TextModel text);
    }

    private List<TextModel> texts;
    private OnItemClickListener listener;

    public TextsAdapter(List<TextModel> texts, OnItemClickListener listener) {
        this.texts = texts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_texts, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextModel text = texts.get(position);
        holder.title.setText(text.getTitle());
        holder.level.setText("Уровень: N" + text.getDifficultyLevel());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(text));
    }

    @Override
    public int getItemCount() {
        return texts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, level;
        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            level = itemView.findViewById(R.id.textLevel);
        }
    }
}
