package com.example.japanese_self_study_guide.grammar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;

import java.util.ArrayList;
import java.util.List;

public class GrammarAdapter extends RecyclerView.Adapter<GrammarAdapter.RuleViewHolder> {

    public interface OnRuleClick {
        void onClick(GrammarRule rule);
    }

    private List<GrammarRule> rules;
    private OnRuleClick listener;
    private List<Integer> learnedIds = new ArrayList<>();
    public GrammarAdapter(List<GrammarRule> rules, OnRuleClick listener) {
        this.rules = rules;
        this.listener = listener;
    }

    public void setLearnedIds(List<Integer> ids) {
        this.learnedIds = ids;
        notifyDataSetChanged();
    }

    public static class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView textStructure;
        ImageView imgLearned;

        public RuleViewHolder(View itemView) {
            super(itemView);
            textStructure = itemView.findViewById(R.id.textStructure);
            imgLearned = itemView.findViewById(R.id.imgLearned);
        }
    }


    @Override
    public RuleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grammar, parent, false);
        return new RuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RuleViewHolder holder, int position) {
        GrammarRule rule = rules.get(position);

        holder.textStructure.setText(rule.getStructure());

        if (learnedIds.contains(rule.getId())) {
            holder.imgLearned.setVisibility(View.VISIBLE);
        } else {
            holder.imgLearned.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onClick(rule));
    }


    @Override
    public int getItemCount() {
        return rules.size();
    }
}

