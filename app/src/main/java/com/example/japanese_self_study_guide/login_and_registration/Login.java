package com.example.japanese_self_study_guide.login_and_registration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.main_profile.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class Login extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin, btnLang;
    private TextView textViewRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale(); // загрузка языка
        super.onCreate(savedInstanceState);



        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), bottom);
                return insets;
            });
        }

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        textViewRegister = findViewById(R.id.text_register);
        progressBar = findViewById(R.id.progressBar);
        btnLang = findViewById(R.id.btn_lang);

        btnLang.setOnClickListener(v -> {
            String currentLang = getResources().getConfiguration().locale.getLanguage();
            String newLang = currentLang.equals("ru") ? "ja" : "ru";
            setLocale(newLang);
        });

        buttonLogin.setOnClickListener(v -> loginUser());
        textViewRegister.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Registration.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }
    }

    private void loginUser() {
        String email = String.valueOf(editTextEmail.getText()).trim();
        String password = String.valueOf(editTextPassword.getText()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Login.this, MainActivity.class));
                                finish();
                            } else {
                                mAuth.signOut();
                                Toast.makeText(this, getString(R.string.email_verification), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Login.this, VerifyEmail.class);
                                intent.putExtra("USER_EMAIL", email);
                                startActivity(intent);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", langCode);
        editor.apply();

        recreate();
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "ru");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
