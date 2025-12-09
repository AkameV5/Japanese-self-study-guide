package com.example.japanese_self_study_guide.main_profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.japanese_self_study_guide.audio.AudioExerciseActivity;
import com.example.japanese_self_study_guide.audio.AudioPlayerActivity;
import com.example.japanese_self_study_guide.grammar.GrammarDetailActivity;
import com.example.japanese_self_study_guide.grammar.GrammarExerciseActivity;
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaExercisesActivity;
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaGroupProvider;
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaGroupsActivity;
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaExercisesActivity;
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaGroupProvider;
import com.example.japanese_self_study_guide.kanji.ExerciseGroup;
import com.example.japanese_self_study_guide.kanji.GroupsProvider;
import com.example.japanese_self_study_guide.kanji.KanjiExercisesActivity;
import com.example.japanese_self_study_guide.main_profile.ProgressManager;
import com.example.japanese_self_study_guide.main_profile.TotalManager;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.audio.AudioActivity;
import com.example.japanese_self_study_guide.dictionary.DictionaryActivity;
import com.example.japanese_self_study_guide.grammar.GrammarActivity;
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaActivity;
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaActivity;
import com.example.japanese_self_study_guide.kanji.KanjiActivity;
import com.example.japanese_self_study_guide.texts_and_translation.TextDetailActivity;
import com.example.japanese_self_study_guide.texts_and_translation.TextsActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth mAuth;

    // 👇 добавляем для анимации
    private View mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.accentPinkDark)); // ← тот же цвет, что у Toolbar
        }

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✔ Добавляем отступ сверху под фронталку, безопасно для всех устройств
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            // если статусбар > 0 — тогда добавляем отступ
            if (statusBarHeight > 0) {
                v.setPadding(
                        v.getPaddingLeft(),
                        statusBarHeight,
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );
            }

            return insets;
        });


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        mainContent = findViewById(R.id.fragment_container);
        loadDailyRecommendations();
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        TextView tvUsername = headerView.findViewById(R.id.tvUsername);
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);
        ImageView ivProfilePic = headerView.findViewById(R.id.ivProfilePic);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String username = task.getResult().getString("username");
                    String email = task.getResult().getString("email");
                    String profilePicUrl = task.getResult().getString("profilePicUrl");

                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)
                            .build();
                    db.setFirestoreSettings(settings);

                    tvUsername.setText(username != null ? username : "Без имени");
                    tvUserEmail.setText(email != null ? email : currentUser.getEmail());

                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Picasso.get().load(profilePicUrl)
                                .placeholder(R.drawable.profile_user_def)
                                .error(R.drawable.profile_user_def)
                                .into(ivProfilePic);
                    } else {
                        ivProfilePic.setImageResource(R.drawable.profile_user_def);
                    }
                } else {
                    Toast.makeText(this, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
                }
            });
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, Profile.class));
            } else if (id == R.id.nav_hiragana) {
                startActivity(new Intent(MainActivity.this, HiraganaActivity.class));
            } else if (id == R.id.nav_katakana) {
                startActivity(new Intent(MainActivity.this, KatakanaActivity.class));
            } else if (id == R.id.nav_kanji) {
                startActivity(new Intent(MainActivity.this, KanjiActivity.class));
            } else if (id == R.id.nav_texts) {
                    startActivity(new Intent(MainActivity.this, TextsActivity.class));
            } else if (id == R.id.nav_dictionary) {
                startActivity(new Intent(MainActivity.this, DictionaryActivity.class));
            } else if (id == R.id.nav_grammar) {
                startActivity(new Intent(MainActivity.this, GrammarActivity.class));
            } else if (id == R.id.nav_audio) {
                startActivity(new Intent(MainActivity.this, AudioActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }


        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                float scale = 1 - (slideOffset * 0.2f);
                float translationX = drawerView.getWidth() * slideOffset;
                mainContent.setTranslationX(translationX);
                mainContent.setScaleX(scale);
                mainContent.setScaleY(scale);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeaderData();
    }

    private void updateHeaderData() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUsername = headerView.findViewById(R.id.tvUsername);
        ImageView ivProfilePic = headerView.findViewById(R.id.ivProfilePic);

        SharedPreferences prefs = getSharedPreferences("LocalUser", MODE_PRIVATE);
        String localName = prefs.getString("username", null);
        String localAvatar = prefs.getString("avatarPath", null);

        if (localName != null) tvUsername.setText(localName);
        if (localAvatar != null && !localAvatar.isEmpty()) {
            ivProfilePic.setImageURI(Uri.fromFile(new File(localAvatar)));
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            ProgressManager.initProgressIfNeeded(user.getUid());
        }
    }
    private void loadDailyRecommendations() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        DocumentReference ref = db.collection("Daily").document(uid);

        ProgressManager.getProgressDoc(uid).addOnSuccessListener(progressDoc -> {
            generateRecommendations(ref, today, progressDoc, null);
        });
    }

    private void generateRecommendations(DocumentReference ref,
                                         String today,
                                         DocumentSnapshot progressDoc,
                                         Map<String, Integer> totals) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(uid).get().addOnSuccessListener(doc -> {

            List<Long> hiraganaLearned = doc.get("hiraganaLearned") == null ? new ArrayList<>() :
                    (List<Long>) doc.get("hiraganaLearned");
            List<Long> katakanaLearned = doc.get("katakanaLearned") == null ? new ArrayList<>() :
                    (List<Long>) doc.get("katakanaLearned");
            List<Long> kanjiLearned = doc.get("kanjiLearned") == null ? new ArrayList<>() :
                    (List<Long>) doc.get("kanjiLearned");
            List<Long> grammarLearned = doc.get("grammarLearned") == null ? new ArrayList<>() :
                    (List<Long>) doc.get("grammarLearned");
            List<Long> textsLearned = doc.get("textsLearned") == null ? new ArrayList<>() :
                    (List<Long>) doc.get("textsLearned");
            List<Long> audioLearned = doc.get("audioLearned") == null ? new ArrayList<>() :
                    (List<Long>) doc.get("audioLearned");

            List<Map<String, Object>> newList = new ArrayList<>();

            // ХИРАГАНА
            List<Integer> hiraIdsForToday = pickNextFromGroups(HiraganaGroupProvider.GROUPS_ALL, hiraganaLearned, 5);
            if (!hiraIdsForToday.isEmpty()) {
                newList.add(makeRecMap("Хирагана", "hiragana",
                        Map.of("ids", hiraIdsForToday)));
            }

            // КАТАКАНА
            List<Integer> kataIdsForToday = pickNextFromGroups(KatakanaGroupProvider.GROUPS_ALL, katakanaLearned, 5);
            if (!kataIdsForToday.isEmpty()) {
                newList.add(makeRecMap("Катакана", "katakana",
                        Map.of("ids", kataIdsForToday)));
            }

            // КАНДЗИ
            List<Integer> kanjiIdsForToday = pickNextKanjiIds(kanjiLearned);
            if (!kanjiIdsForToday.isEmpty()) {
                ExerciseGroup group = findGroupForIds(kanjiIdsForToday);
                newList.add(makeRecMap("Кандзи", "kanji",
                        Map.of(
                                "ids", kanjiIdsForToday,
                                "startId", group.getStartId(),
                                "endId", group.getEndId(),
                                "limit", group.getLimit()
                        )));

            }

            // ГРАММАТИКА
            Long grammarId = pickNextSingleId(grammarLearned, "Grammar");
            if (grammarId != null) {
                newList.add(makeRecMap("Грамматика", "grammar",
                        Map.of("id", grammarId.intValue())));
            }

            // ТЕКСТ (асинхронный)
            pickNextTextIdByLevel(textsLearned, textId -> {
                if (textId != null) {
                    newList.add(makeRecMap("Текст", "text",
                            Map.of("id", textId.intValue())));
                }

                // АУДИО
                Long audioId = pickNextSingleId(audioLearned, "Audio");
                if (audioId != null) {
                    newList.add(makeRecMap("Аудио", "audio",
                            Map.of("id", audioId.intValue())));
                }

                saveDailyAndShow(ref, today, newList);
            });

        });
    }


    private Map<String,Object> makeRecMap(String title, String type, Map<String,Object> payload){
        Map<String,Object> m = new HashMap<>();
        m.put("title", title);
        m.put("type", type);
        m.put("payload", payload);

        Map<String,Object> meta = new HashMap<>();
        if (payload.containsKey("ids")) {
            List<Integer> ids = (List<Integer>) payload.get("ids");
            meta.put("count", ids.size());
        }
        if (payload.containsKey("id")) {
            meta.put("id", payload.get("id"));
        }
        m.put("meta", meta);

        return m;
    }
    private List<Integer> pickNextFromGroups(int[][] GROUPS_ALL, List<Long> learnedLongs, int N) {
        java.util.Set<Integer> learned = new java.util.HashSet<>();
        for (Long l : learnedLongs) learned.add(l.intValue());
        for (int[] group : GROUPS_ALL) {
            List<Integer> notLearned = new ArrayList<>();
            for (int id : group) {
                if (!learned.contains(id)) {
                    notLearned.add(id);
                }
            }
            if (!notLearned.isEmpty()) {
                if (notLearned.size() > N)
                    return notLearned.subList(0, N);
                return notLearned;
            }
        }
        return new ArrayList<>();
    }

    private List<Integer> pickNextKanjiIds(List<Long> learnedLongs) {
        Set<Integer> learned = new HashSet<>();
        for (Long l : learnedLongs) learned.add(l.intValue());

        List<ExerciseGroup> groups = GroupsProvider.getGroups();

        for (ExerciseGroup g : groups) {
            List<Integer> res = new ArrayList<>();

            for (int id = g.getStartId(); id <= g.getEndId(); id++) {
                if (!learned.contains(id)) {
                    res.add(id);
                    if (res.size() >= g.getLimit()) {
                        return res;
                    }
                }
            }

            if (!res.isEmpty()) {
                return res; // ✅ берём ТОЛЬКО ОДНУ группу
            }
        }

        return new ArrayList<>();
    }

    private Long pickNextSingleId(List<Long> learnedLongs, String collectionName) {
        for (int id = 1; id <= 500; id++) {
            if (!learnedLongs.contains((long)id)) {
                return (long) id;
            }
        }
        return null;
    }
    private ExerciseGroup findGroupForIds(List<Integer> ids) {
        int first = ids.get(0);
        for (ExerciseGroup g : GroupsProvider.getGroups()) {
            if (first >= g.getStartId() && first <= g.getEndId()) {
                return g;
            }
        }
        return null;
    }

    // Выбор текста по уровню N5->N1. Проверяет поле "level" в коллекции Texts и возвращает первый текстId, которого нет в textsLearned
    private void pickNextTextIdByLevel(List<Long> learned, OnTextFound callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Texts").get().addOnSuccessListener(query -> {
            Long best = null;
            int bestLevel = 999;

            for (var doc : query) {
                Long id = doc.getLong("id");
                if (id == null) continue;

                if (learned.contains(id)) continue;

                String raw = doc.getString("difficultyLevel");
                int lvl = parseLevel(raw);

                if (lvl < bestLevel) {
                    bestLevel = lvl;
                    best = id;
                }
            }

            callback.onFound(best);

        }).addOnFailureListener(e -> callback.onFound(null));
    }

    private int parseLevel(String raw) {
        if (raw == null) return 999;
        raw = raw.trim().toUpperCase();

        if (raw.contains("N5") || raw.equals("5") || raw.equals("N 5")) return 1;
        if (raw.contains("N4") || raw.equals("4")) return 2;
        if (raw.contains("N3") || raw.equals("3")) return 3;
        if (raw.contains("N2") || raw.equals("2")) return 4;
        if (raw.contains("N1") || raw.equals("1")) return 5;

        return 999;
    }

    interface OnTextFound {
        void onFound(Long id);
    }


    private void showRecommendations(List<Map<String, Object>> list) {
        LinearLayout container = findViewById(R.id.recommendations_list);
        container.removeAllViews();

        for (Map<String, Object> rec : list) {
            View item = getLayoutInflater().inflate(R.layout.item_recommendation, container, false);

            TextView title = item.findViewById(R.id.rec_title);
            TextView subtitle = item.findViewById(R.id.rec_subtitle); // добавь текстовое поле в item_recommendation

            String titleTxt = rec.get("title").toString();
            title.setText(titleTxt);

            Map<String, Object> meta = (Map<String, Object>) rec.get("meta");
            String subtitleTxt = buildSubtitleForMeta(rec);
            if (subtitle != null) subtitle.setText(subtitleTxt);

            item.setOnClickListener(v -> {
                openRecommendedLesson(rec);
            });

            container.addView(item);
        }
    }

    private String buildSubtitleForMeta(Map<String, Object> rec) {
        if (rec == null) return "";

        Map<String, Object> meta = (Map<String, Object>) rec.get("meta");
        String type = (String) rec.get("type");

        if (meta == null) return "";

        switch (type) {

            case "grammar":
                return "Изучить новую грамматику";

            case "text":
                return "Прочитать новый текст";

            case "audio":
                return "Прослушать новый аудиофайл";

            case "hiragana":
                return "Сегодня " + meta.get("count") + " символов хираганы";

            case "katakana":
                return "Сегодня " + meta.get("count") + " символов катаканы";

            case "kanji":
                return "Сегодня " + meta.get("count") + " кандзи";

            default:
                return "";
        }
    }

    private void openRecommendedLesson(Map<String, Object> rec) {
        String type = (String) rec.get("type");
        Map<String,Object> payload = (Map<String,Object>) rec.get("payload");

        Intent intent = null;

        switch (type) {

            // -------------------- HIRAGANA --------------------
            case "hiragana": {
                intent = new Intent(this, HiraganaActivity.class);
                List<Integer> idsList = (List<Integer>) payload.get("ids");
                if (idsList != null) {
                    int[] arr = new int[idsList.size()];
                    for (int i = 0; i < idsList.size(); i++) arr[i] = idsList.get(i);
                    intent.putExtra("daily_hiragana_ids", arr);
                }
                intent.putExtra("daily_mode", true);
                break;
            }
            case "katakana": {
                intent = new Intent(this, KatakanaActivity.class);
                List<Integer> idsList = (List<Integer>) payload.get("ids");

                if (idsList != null) {
                    int[] arr = new int[idsList.size()];
                    for (int i = 0; i < idsList.size(); i++) arr[i] = idsList.get(i);
                    intent.putExtra("daily_katakana_ids", arr);
                }
                intent.putExtra("daily_mode", true);
                break;
            }

            case "kanji": {
                intent = new Intent(this, KanjiActivity.class);
                List<Integer> idsList = (List<Integer>) payload.get("ids");

                if (idsList != null) {
                    int[] arr = new int[idsList.size()];
                    for (int i = 0; i < idsList.size(); i++) arr[i] = idsList.get(i);
                    intent.putExtra("daily_kanji_ids", arr);
                }
                intent.putExtra("daily_start_id", (int)payload.get("startId"));
                intent.putExtra("daily_end_id", (int)payload.get("endId"));
                intent.putExtra("daily_limit", (int)payload.get("limit"));
                intent.putExtra("daily_mode", true);

                break;
            }

            // -------------------- GRAMMAR --------------------
            case "grammar": {
                intent = new Intent(this, GrammarDetailActivity.class);
                Integer id = (Integer) payload.get("id");
                if (id != null) {
                    intent.putExtra("daily_mode", true);
                    intent.putExtra("id", id);
                }
                break;
            }

            case "text": {
                intent = new Intent(this, TextDetailActivity.class);
                Integer id = (Integer) payload.get("id");
                if (id != null) intent.putExtra("textId", id);
                intent.putExtra("daily_mode", true);
                break;
            }


            case "audio": {
                Integer id = (Integer) payload.get("id");
                if (id == null) return;

                FirebaseFirestore.getInstance()
                        .collection("Audio")
                        .whereEqualTo("id", id)
                        .get()
                        .addOnSuccessListener(query -> {
                            if (!query.isEmpty()) {
                                var doc = query.getDocuments().get(0);

                                Intent i = new Intent(this, AudioPlayerActivity.class);
                                i.putExtra("audioId", id);
                                i.putExtra("audio_url", doc.getString("url"));
                                i.putExtra("audio_name", doc.getString("name"));
                                i.putExtra("audio_description", doc.getString("description"));
                                i.putExtra("daily_mode", true);

                                startActivity(i);
                            }
                        });
                return; //
            }

        }

        if (intent != null) startActivity(intent);
    }

    private void saveDailyAndShow(DocumentReference ref, String today, List<Map<String,Object>> list) {

        Map<String, Object> data = new HashMap<>();
        data.put("date", today);
        data.put("recommendations", list);

        ref.set(data).addOnSuccessListener(unused -> {
            showRecommendations(list);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Ошибка сохранения daily", Toast.LENGTH_SHORT).show();
        });
    }

}


