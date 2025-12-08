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
import android.widget.TextView;
import android.widget.Toast;
import com.example.japanese_self_study_guide.main_profile.ProgressManager;

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
import com.example.japanese_self_study_guide.texts_and_translation.TextsActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.squareup.picasso.Picasso;

import java.io.File;

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


}
