package com.example.japanese_self_study_guide.hiragana_katakana;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;

import java.util.ArrayList;
import java.util.List;

public class HiraganaAdapter extends RecyclerView.Adapter<HiraganaAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(HiraganaItem item);
    }
    private List<Integer> learnedIds = new ArrayList<>();
    public void setLearnedIds(List<Integer> ids) {
        this.learnedIds = ids;
        notifyDataSetChanged();
    }
    private final List<HiraganaItem> list;
    private final OnItemClickListener listener;
    public HiraganaAdapter(List<HiraganaItem> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hiragana, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HiraganaItem item = list.get(position);

        holder.tvSymbol.setText(item.getSymbol());
        holder.tvRomaji.setText(item.getRomaji());

        holder.itemView.setOnClickListener(v -> {
            if (item.getId() != -1) listener.onClick(item);
        });

        if (learnedIds.contains(item.getId())) {
            holder.imgLearned.setVisibility(View.VISIBLE);
        } else {
            holder.imgLearned.setVisibility(View.GONE);
        }

    }
    @Override
    public int getItemCount() { return list.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymbol, tvRomaji;
        ImageView imgLearned;
        ViewHolder(View itemView) {
            super(itemView);
            tvSymbol = itemView.findViewById(R.id.tvSymbol);
            tvRomaji = itemView.findViewById(R.id.tvRomaji);
            imgLearned = itemView.findViewById(R.id.imgLearned);
        }
    }

}
