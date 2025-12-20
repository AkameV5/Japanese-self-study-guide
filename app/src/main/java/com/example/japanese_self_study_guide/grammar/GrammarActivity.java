package com.example.japanese_self_study_guide.grammar;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.main_profile.ProgressManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class GrammarActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GrammarAdapter adapter;
    private List<GrammarRule> rules = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar);

        recyclerView = findViewById(R.id.recyclerViewGrammar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GrammarAdapter(rules, rule -> openRule(rule));
        recyclerView.setAdapter(adapter);

        loadRules();

        ProgressManager.getProgressDoc(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addOnSuccessListener(doc -> {
                    List<Long> learned = (List<Long>) doc.get("grammarLearned");
                    if (learned == null) return;

                    List<Integer> ids = new ArrayList<>();
                    for (Long l : learned) ids.add(l.intValue());

                    adapter.setLearnedIds(ids);
                });

    }

    private void loadRules() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Grammar")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        rules.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            GrammarRule r = doc.toObject(GrammarRule.class);
                            rules.add(r);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void openRule(GrammarRule rule) {
        Intent intent = new Intent(this, GrammarDetailActivity.class);
        intent.putExtra("id", rule.getId());
        intent.putExtra("structure", rule.getStructure());
        intent.putExtra("explanation", rule.getExplanation());
        intent.putExtra("example", rule.getExample());
        intent.putExtra("translation", rule.getTranslation());
        startActivity(intent);
    }
}

