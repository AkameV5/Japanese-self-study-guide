package com.example.japanese_self_study_guide.audio;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.main_profile.ProgressManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AudioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AudioAdapter adapter;
    private ArrayList<AudioModel> audioList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        recyclerView = findViewById(R.id.audioRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AudioAdapter(this, audioList);
        ProgressManager.getProgressDoc(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addOnSuccessListener(doc -> {
                    List<Long> learned = (List<Long>) doc.get("audioLearned");
                    if (learned == null) return;

                    List<Integer> ids = new ArrayList<>();
                    for (Long l : learned) ids.add(l.intValue());

                    adapter.setLearnedIds(ids);
                });

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadAudioList();
    }

    private void loadAudioList() {
        db.collection("Audio")
                .get()
                .addOnSuccessListener(query -> {
                    audioList.clear();
                    for (var doc : query) {
                        AudioModel audio = doc.toObject(AudioModel.class);
                        audioList.add(audio);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
