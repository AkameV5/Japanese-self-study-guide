package com.example.japanese_self_study_guide.main_profile;

import android.app.AlarmManager;
import android.app.PendingIntent;
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

import com.example.japanese_self_study_guide.audio.AudioPlayerActivity;
import com.example.japanese_self_study_guide.grammar.GrammarDetailActivity;
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaGroupProvider;
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaGroupProvider;
import com.example.japanese_self_study_guide.kanji.ExerciseGroup;
import com.example.japanese_self_study_guide.kanji.GroupsProvider;
import com.example.japanese_self_study_guide.kanji.KanjiExercisesActivity;
import com.example.japanese_self_study_guide.login_and_registration.Login;
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
import com.google.firebase.firestore.ListenerRegistration;
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
    private ListenerRegistration dailyListener;
    private View mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.accentPinkDark));
        }

        mAuth = FirebaseAuth.getInstance();
        scheduleDailyReminder();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

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

        LinearLayout logoutContainer = navigationView.findViewById(R.id.nav_logout_container);
        logoutContainer.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });


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
        DocumentReference ref = db.collection("Daily").document(uid);

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        dailyListener = ref.addSnapshotListener((doc, e) -> {
            if (e != null) return;
            if (doc == null || !doc.exists()) {
                generateRecommendations(ref, today, null, null);
                return;
            }

            String savedDate = doc.getString("date");
            List<Map<String, Object>> list =
                    (List<Map<String, Object>>) doc.get("recommendations");

            if (savedDate == null || !savedDate.equals(today)) {
                generateRecommendations(ref, today, doc, null);
                return;
            }

            if (list != null && !list.isEmpty()) {
                showRecommendations(list);
                return;
            }
            showRecommendations(new ArrayList<>());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dailyListener != null) {
            dailyListener.remove();
            dailyListener = null;
        }
    }

    private void generateRecommendations(DocumentReference ref,
                                         String today,
                                         DocumentSnapshot progressDoc,
                                         Map<String, Integer> totals) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Progress").document(uid).get().addOnSuccessListener(doc -> {

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

            List<Integer> hiraIdsForToday = pickNextFromGroups(HiraganaGroupProvider.GROUPS_ALL, hiraganaLearned, 5);
            if (!hiraIdsForToday.isEmpty()) {
                newList.add(makeRecMap("Хирагана", "hiragana",
                        Map.of("ids", hiraIdsForToday)));
            }

            List<Integer> kataIdsForToday = pickNextFromGroups(KatakanaGroupProvider.GROUPS_ALL, katakanaLearned, 5);
            if (!kataIdsForToday.isEmpty()) {
                newList.add(makeRecMap("Катакана", "katakana",
                        Map.of("ids", kataIdsForToday)));
            }

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

            Long grammarId = pickNextSingleId(grammarLearned, "Grammar");
            if (grammarId != null) {
                newList.add(makeRecMap("Грамматика", "grammar",
                        Map.of("id", grammarId.intValue())));
            }

            pickNextTextIdByLevel(textsLearned, textId -> {
                if (textId != null) {
                    newList.add(makeRecMap("Текст", "text",
                            Map.of("id", textId.intValue())));
                }

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
                return res;
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

        if (list.isEmpty()) {
            View item = getLayoutInflater().inflate(R.layout.item_recommendation, container, false);

            TextView title = item.findViewById(R.id.rec_title);
            TextView subtitle = item.findViewById(R.id.rec_subtitle);

            title.setText("Все рекомендации выполнены!");
            if (subtitle != null) subtitle.setText("Отличная работа!");

            container.addView(item);
            return;
        }

        for (Map<String, Object> rec : list) {
            View item = getLayoutInflater().inflate(R.layout.item_recommendation, container, false);

            TextView title = item.findViewById(R.id.rec_title);
            TextView subtitle = item.findViewById(R.id.rec_subtitle);

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

            case "hiragana": {
                intent = new Intent(this, HiraganaActivity.class);
                List<?> idsRaw = (List<?>) payload.get("ids");

                if (idsRaw != null) {
                    int[] arr = new int[idsRaw.size()];
                    for (int i = 0; i < idsRaw.size(); i++) {
                        Object v = idsRaw.get(i);
                        arr[i] = (v instanceof Long) ? ((Long) v).intValue() : (int) v;
                    }
                    intent.putExtra("daily_hiragana_ids", arr);
                }

                intent.putExtra("daily_mode", true);
                break;
            }

            case "katakana": {
                intent = new Intent(this, KatakanaActivity.class);
                List<?> idsRaw = (List<?>) payload.get("ids");

                if (idsRaw != null) {
                    int[] arr = new int[idsRaw.size()];
                    for (int i = 0; i < idsRaw.size(); i++) {
                        Object v = idsRaw.get(i);
                        arr[i] = (v instanceof Long) ? ((Long) v).intValue() : (int) v;
                    }
                    intent.putExtra("daily_katakana_ids", arr);
                }
                intent.putExtra("daily_mode", true);
                break;
            }

            case "kanji": {
                intent = new Intent(this, KanjiActivity.class);
                List<?> idsRaw = (List<?>) payload.get("ids");

                if (idsRaw != null) {
                    int[] arr = new int[idsRaw.size()];
                    for (int i = 0; i < idsRaw.size(); i++) {
                        Object v = idsRaw.get(i);
                        arr[i] = (v instanceof Long) ? ((Long) v).intValue() : (int) v;
                    }
                    intent.putExtra("daily_kanji_ids", arr);
                }

                intent.putExtra("daily_start_id", ((Long) payload.get("startId")).intValue());
                intent.putExtra("daily_end_id", ((Long) payload.get("endId")).intValue());
                intent.putExtra("daily_limit", ((Long) payload.get("limit")).intValue());
                intent.putExtra("daily_mode", true);
                break;
            }

            case "grammar": {
                intent = new Intent(this, GrammarDetailActivity.class);
                Long id = (Long) payload.get("id");
                if (id != null) {
                    intent.putExtra("id", id.intValue());
                    intent.putExtra("daily_mode", true);
                }
                break;
            }

            case "text": {
                intent = new Intent(this, TextDetailActivity.class);
                Long id = (Long) payload.get("id");
                if (id != null) {
                    intent.putExtra("textId", id.intValue());
                }
                intent.putExtra("daily_mode", true);
                break;
            }

            case "audio": {
                Long id = (Long) payload.get("id");
                if (id == null) return;

                FirebaseFirestore.getInstance()
                        .collection("Audio")
                        .whereEqualTo("id", id.intValue())
                        .get()
                        .addOnSuccessListener(query -> {
                            if (!query.isEmpty()) {
                                var doc = query.getDocuments().get(0);

                                Intent i = new Intent(this, AudioPlayerActivity.class);
                                i.putExtra("audioId", id.intValue());
                                i.putExtra("audio_url", doc.getString("url"));
                                i.putExtra("audio_name", doc.getString("name"));
                                i.putExtra("audio_description", doc.getString("description"));
                                i.putExtra("daily_mode", true);

                                startActivity(i);
                            }
                        });
                return;
            }
        }

        if (intent != null) startActivity(intent);
    }


    private void saveDailyAndShow(DocumentReference ref, String today, List<Map<String,Object>> list) {

        Map<String, Object> data = new HashMap<>();
        data.put("date", today);
        data.put("recommendations", list);
        ref.set(data);
    }
    public static void removeDailyRecommendation(String type, int id) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("Daily").document(uid);

        db.runTransaction(transaction -> {
            DocumentSnapshot doc = transaction.get(ref);
            if (!doc.exists()) return null;

            List<Map<String, Object>> list =
                    (List<Map<String, Object>>) doc.get("recommendations");
            if (list == null) return null;

            List<Map<String, Object>> newList = new ArrayList<>();

            for (Map<String, Object> rec : list) {
                if (!type.equals(rec.get("type"))) {
                    newList.add(rec);
                    continue;
                }

                Map<String, Object> payload =
                        (Map<String, Object>) rec.get("payload");

                switch (type) {
                    case "hiragana":
                    case "katakana":
                    case "kanji": {
                        List<Long> ids = (List<Long>) payload.get("ids");
                        if (ids == null || !ids.contains((long) id)) {
                            newList.add(rec);
                        }
                        break;
                    }
                    default: {
                        Long v = (Long) payload.get("id");
                        if (v == null || v != id) {
                            newList.add(rec);
                        }
                    }
                }
            }
            transaction.update(ref, "recommendations", newList);
            return null;
        });
    }

    private void scheduleDailyReminder() {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

        target.set(Calendar.HOUR_OF_DAY, 18);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);

        if (now.after(target)) {
            target.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, DailyReminderWorker.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                target.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

}


