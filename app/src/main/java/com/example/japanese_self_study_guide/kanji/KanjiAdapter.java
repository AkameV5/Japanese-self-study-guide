package com.example.japanese_self_study_guide.kanji;

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

public class KanjiAdapter extends RecyclerView.Adapter<KanjiAdapter.KanjiViewHolder> {

    private List<KanjiModel> kanjiList;
    private List<KanjiModel> fullList;
    private List<Integer> learnedIds = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(KanjiModel model);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public KanjiAdapter(List<KanjiModel> kanjiList) {
        this.kanjiList = new ArrayList<>(kanjiList);
        this.fullList = new ArrayList<>(kanjiList);
    }

    public void setLearnedIds(List<Integer> ids) {
        this.learnedIds = ids;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KanjiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kanji, parent, false);
        return new KanjiViewHolder(view, listener, kanjiList);
    }


    @Override
    public void onBindViewHolder(@NonNull KanjiViewHolder holder, int position) {
        KanjiModel item = kanjiList.get(position);
        holder.textKanji.setText(item.getKanji());
        holder.textMeaning.setText("Значение: " + item.getMeaning());
        holder.textOnYomi.setText("Онъёми: " + String.join(", ", item.getOnYomi()));
        holder.textKunYomi.setText("Кунъёми: " + String.join(", ", item.getKunYomi()));
        holder.textJlpt.setText("JLPT: N" + item.getJlpt());
        holder.textCategory.setText("Категория: " + item.getCategory());

        if (learnedIds.contains((int) item.getId())) {
            holder.imgLearned.setVisibility(View.VISIBLE);
        } else {
            holder.imgLearned.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return kanjiList.size();
    }

    public void updateList(List<KanjiModel> newList) {
        kanjiList.clear();
        kanjiList.addAll(newList);
        fullList.clear();
        fullList.addAll(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        List<KanjiModel> filtered = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            filtered.addAll(fullList);
        } else {
            String query = text.toLowerCase();
            for (KanjiModel k : fullList) {
                if (k.getKanji().contains(query)
                        || k.getMeaning().toLowerCase().contains(query)
                        || k.getCategory().toLowerCase().contains(query)) {
                    filtered.add(k);
                }
            }
        }
        updateList(filtered);
    }

    static class KanjiViewHolder extends RecyclerView.ViewHolder {
        TextView textKanji, textMeaning, textOnYomi, textKunYomi, textJlpt, textCategory; ImageView imgLearned;

        KanjiViewHolder(View itemView, OnItemClickListener listener, List<KanjiModel> list) {
            super(itemView);

            textKanji = itemView.findViewById(R.id.textKanji);
            textMeaning = itemView.findViewById(R.id.textMeaning);
            textOnYomi = itemView.findViewById(R.id.textOnYomi);
            textKunYomi = itemView.findViewById(R.id.textKunYomi);
            textJlpt = itemView.findViewById(R.id.textJlpt);
            textCategory = itemView.findViewById(R.id.textCategory);
            imgLearned = itemView.findViewById(R.id.imgLearned);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(list.get(pos));
                }
            });
        }
    }

}
