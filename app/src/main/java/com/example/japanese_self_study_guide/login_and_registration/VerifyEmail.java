package com.example.japanese_self_study_guide.login_and_registration;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.main_profile.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Handler handler;
    private Runnable checkEmailVerifiedRunnable;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_verify_email);

        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        View root = findViewById(R.id.verify_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                v.setPadding(v.getPaddingLeft(), topInset, v.getPaddingRight(), bottomInset);
                return insets;
            });
        }

        mAuth = FirebaseAuth.getInstance();

        Button btnResendEmail = findViewById(R.id.btnResendEmail);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);
        TextView tvVerifyMessage = findViewById(R.id.tvVerifyMessage);

        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (!TextUtils.isEmpty(userEmail)) {
            tvVerifyMessage.setText(getString(R.string.verify_email_message, userEmail));
        }

        btnResendEmail.setOnClickListener(v -> resendVerificationEmail());
        tvBackToLogin.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(VerifyEmail.this, Login.class));
            finish();
        });

        startEmailVerificationCheck();
    }

    private void startEmailVerificationCheck() {
        handler = new Handler();
        checkEmailVerifiedRunnable = new Runnable() {
            public void run() {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    user.reload().addOnCompleteListener(task -> {
                        if (user.isEmailVerified()) {
                            Toast.makeText(VerifyEmail.this, "Почта подтверждена!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(VerifyEmail.this, MainActivity.class));
                            finish();
                        } else {
                            handler.postDelayed(this, 3000);
                        }
                    });
                }
            }
        };
        handler.post(checkEmailVerifiedRunnable);
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Письмо отправлено повторно.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Не удалось отправить письмо.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    protected void onStop() {
        super.onStop();
        if (handler != null && checkEmailVerifiedRunnable != null) {
            handler.removeCallbacks(checkEmailVerifiedRunnable);
        }
    }
}