package com.example.japanese_self_study_guide.texts_and_translation;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.japanese_self_study_guide.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ExercisesAdapter extends RecyclerView.Adapter<ExercisesAdapter.ViewHolder> {

    private List<ExerciseModel> list;

    public ExercisesAdapter(List<ExerciseModel> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_text_exercise, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseModel e = list.get(position);
        holder.question.setText(e.getQuestion());
        holder.difficulty.setText("type: " + e.getType());

        holder.buttonAnswer.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), ExerciseDetailActivity.class);
            i.putExtra("exerciseId", e.getId());
            i.putExtra("textId", e.getTextId());
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView question, difficulty;
        MaterialButton buttonAnswer;
        ViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.exerciseQuestion);
            difficulty = itemView.findViewById(R.id.exerciseDifficulty);
            buttonAnswer = itemView.findViewById(R.id.buttonAnswer);
        }
    }
}
