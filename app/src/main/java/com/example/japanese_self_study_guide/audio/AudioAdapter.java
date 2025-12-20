package com.example.japanese_self_study_guide.audio;

import android.content.Context;
import android.content.Intent;
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

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    private Context context;
    private ArrayList<AudioModel> audioList;

    private List<Integer> learnedIds = new ArrayList<>();
    public void setLearnedIds(List<Integer> ids) {
        this.learnedIds = ids;
        notifyDataSetChanged();
    }

    public AudioAdapter(Context context, ArrayList<AudioModel> audioList) {
        this.context = context;
        this.audioList = audioList;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioModel audio = audioList.get(position);

        holder.title.setText(audio.getName());
        holder.description.setText(audio.getDescription());

        if (learnedIds.contains(audio.getId())) {
            holder.imgLearned.setVisibility(View.VISIBLE);
        } else {
            holder.imgLearned.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AudioPlayerActivity.class);
            intent.putExtra("audio_url", audio.getUrl());
            intent.putExtra("audio_name", audio.getName());
            intent.putExtra("audio_description", audio.getDescription());
            intent.putExtra("audioId", audio.getId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {

        TextView title, description;
        ImageView imgLearned;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.audioTitle);
            description = itemView.findViewById(R.id.audioDescription);
            imgLearned = itemView.findViewById(R.id.imgLearned);
        }
    }

}
