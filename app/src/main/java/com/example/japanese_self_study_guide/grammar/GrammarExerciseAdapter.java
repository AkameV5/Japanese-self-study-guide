package com.example.japanese_self_study_guide.grammar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;

import java.util.List;

public class GrammarExerciseAdapter extends RecyclerView.Adapter<GrammarExerciseAdapter.ExViewHolder> {

    private List<GrammarExercise> exercises;

    public interface OnExerciseClick {
        void onClick(GrammarExercise exercise);
    }
    private OnExerciseClick listener;

    public GrammarExerciseAdapter(List<GrammarExercise> exercises, OnExerciseClick listener) {
        this.exercises = exercises;
        this.listener = listener;
    }


    public static class ExViewHolder extends RecyclerView.ViewHolder {
        TextView textTask;

        public ExViewHolder(View itemView) {
            super(itemView);
            textTask = itemView.findViewById(R.id.textTask);
        }
    }

    @Override
    public ExViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_grammar, parent, false);
        return new ExViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExViewHolder holder, int position) {
        holder.textTask.setText(exercises.get(position).getTask());
        holder.itemView.setOnClickListener(v -> listener.onClick(exercises.get(position)));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }
}
