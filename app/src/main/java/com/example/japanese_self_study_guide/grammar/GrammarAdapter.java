package com.example.japanese_self_study_guide.grammar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;

import java.util.List;

public class GrammarAdapter extends RecyclerView.Adapter<GrammarAdapter.RuleViewHolder> {

    public interface OnRuleClick {
        void onClick(GrammarRule rule);
    }

    private List<GrammarRule> rules;
    private OnRuleClick listener;

    public GrammarAdapter(List<GrammarRule> rules, OnRuleClick listener) {
        this.rules = rules;
        this.listener = listener;
    }

    public static class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView textStructure;

        public RuleViewHolder(View itemView) {
            super(itemView);
            textStructure = itemView.findViewById(R.id.textStructure);
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

        holder.itemView.setOnClickListener(v -> listener.onClick(rule));
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }
}

