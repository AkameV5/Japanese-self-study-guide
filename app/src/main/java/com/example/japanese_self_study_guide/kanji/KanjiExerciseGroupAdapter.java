package com.example.japanese_self_study_guide.kanji;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;

import java.util.List;

public class KanjiExerciseGroupAdapter
        extends RecyclerView.Adapter<KanjiExerciseGroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(ExerciseGroup group);
    }

    private List<ExerciseGroup> groups;
    private OnGroupClickListener listener;

    public KanjiExerciseGroupAdapter(List<ExerciseGroup> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        ExerciseGroup group = groups.get(position);
        holder.title.setText(group.getTitle());

        holder.card.setOnClickListener(v -> listener.onGroupClick(group));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        CardView card;

        public GroupViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvKanjiGroupTitle);
            card = itemView.findViewById(R.id.cardGroup);
        }
    }
}
