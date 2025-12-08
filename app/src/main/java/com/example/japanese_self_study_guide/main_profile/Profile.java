package com.example.japanese_self_study_guide.main_profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.audio.AudioActivity;
import com.example.japanese_self_study_guide.grammar.GrammarActivity;
import com.example.japanese_self_study_guide.hiragana_katakana.HiraganaActivity;
import com.example.japanese_self_study_guide.hiragana_katakana.KatakanaActivity;
import com.example.japanese_self_study_guide.kanji.KanjiActivity;
import com.example.japanese_self_study_guide.main_profile.ProgressRingsView;
import com.example.japanese_self_study_guide.R;
import com.example.japanese_self_study_guide.texts_and_translation.TextsActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private TextInputEditText editTextUsername;
    private ImageView ivProfilePic;
    private Button buttonSave;
    private Uri imageUri;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        loadProgressAndTotals();

        editTextUsername = findViewById(R.id.editTextUsername);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        buttonSave = findViewById(R.id.buttonSave);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadLocalData();
            loadUserData(currentUserId);
        }

        ivProfilePic.setOnClickListener(v -> openImageChooser());
        buttonSave.setOnClickListener(v -> {
            String newUsername = editTextUsername.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                updateUserData(newUsername);
            } else {
                Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
            }
        });

        ProgressRingsView ringsView = findViewById(R.id.ringsView);

        ringsView.setOnRingClickListener(index -> {
            Intent intent;

            switch (index) {
                case 0:
                    intent = new Intent(this, HiraganaActivity.class);
                    break;
                case 1:
                    intent = new Intent(this, KatakanaActivity.class);
                    break;
                case 2:
                    intent = new Intent(this, KanjiActivity.class);
                    break;
                case 3:
                    intent = new Intent(this, GrammarActivity.class);
                    break;
                case 4:
                    intent = new Intent(this, TextsActivity.class);
                    break;
                case 5:
                    intent = new Intent(this, AudioActivity.class);
                    break;
                default:
                    return;
            }

            startActivity(intent);
        });

    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Выберите фото"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri sourceUri = data.getData();
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_avatar.jpg"));

            UCrop.Options options = new UCrop.Options();
            options.setCircleDimmedLayer(true);
            options.setShowCropGrid(false);
            options.setShowCropFrame(false);
            options.setToolbarColor(getResources().getColor(android.R.color.white));
            options.setStatusBarColor(getResources().getColor(android.R.color.white));
            options.setActiveControlsWidgetColor(getResources().getColor(R.color.accentPink));

            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(512, 512)
                    .withOptions(options)
                    .start(this);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            Uri croppedUri = UCrop.getOutput(data);
            if (croppedUri != null) {
                imageUri = croppedUri;
                ivProfilePic.setImageURI(croppedUri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR && data != null) {
            Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Ошибка обрезки: " + cropError, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserData(String newUsername) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Сохранение...");
        progressDialog.show();

        DocumentReference userRef = db.collection("Users").document(currentUserId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);

        if (imageUri != null) {
            StorageReference imgRef = storageRef.child("profile_pics/" + UUID.randomUUID().toString());
            imgRef.putFile(imageUri)
                    .addOnSuccessListener(task -> imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("profilePicUrl", uri.toString());
                        saveToFirestore(userRef, updates, progressDialog);
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveToFirestore(userRef, updates, progressDialog);
        }
    }

    private void saveToFirestore(DocumentReference userRef, Map<String, Object> updates, ProgressDialog dialog) {
        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (!isFinishing() && dialog.isShowing()) dialog.dismiss();

                    Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show();

                    // Обновляем UI
                    editTextUsername.setText(updates.get("username").toString());
                    if (imageUri != null) ivProfilePic.setImageURI(imageUri);

                    saveLocalData(updates.get("username").toString(),
                            imageUri != null ? imageUri.getPath() : "");

                    // ✅ Теперь переходим в MainActivity только после успешного обновления
                    Intent intent = new Intent(Profile.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing() && dialog.isShowing()) dialog.dismiss();
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveLocalData(String username, String avatarPath) {
        SharedPreferences prefs = getSharedPreferences("LocalUser", MODE_PRIVATE);
        prefs.edit()
                .putString("username", username)
                .putString("avatarPath", avatarPath)
                .apply();
    }

    private void loadLocalData() {
        SharedPreferences prefs = getSharedPreferences("LocalUser", MODE_PRIVATE);
        String localName = prefs.getString("username", null);
        String localAvatar = prefs.getString("avatarPath", null);

        if (localName != null) editTextUsername.setText(localName);
        if (localAvatar != null && !localAvatar.isEmpty())
            ivProfilePic.setImageURI(Uri.fromFile(new File(localAvatar)));
    }

    private void loadUserData(String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        String profilePicUrl = doc.getString("profilePicUrl");

                        if (username != null) editTextUsername.setText(username);
                        if (profilePicUrl != null && !profilePicUrl.isEmpty())
                            Picasso.get().load(profilePicUrl).into(ivProfilePic);
                    }
                });
    }

    private void loadProgressAndTotals() {

        String uid = mAuth.getCurrentUser().getUid();

        ProgressManager.getProgressDoc(uid)
                .addOnSuccessListener(progressDoc -> {

                    Map<String, Long> progress = new HashMap<>();
                    progress.put("hiraganaDone", progressDoc.getLong("hiraganaDone") == null ? 0 : progressDoc.getLong("hiraganaDone"));
                    progress.put("katakanaDone", progressDoc.getLong("katakanaDone") == null ? 0 : progressDoc.getLong("katakanaDone"));
                    progress.put("kanjiDone", progressDoc.getLong("kanjiDone") == null ? 0 : progressDoc.getLong("kanjiDone"));
                    progress.put("grammarDone", progressDoc.getLong("grammarDone") == null ? 0 : progressDoc.getLong("grammarDone"));
                    progress.put("textsDone", progressDoc.getLong("textsDone") == null ? 0 : progressDoc.getLong("textsDone"));
                    progress.put("audioDone", progressDoc.getLong("audioDone") == null ? 0 : progressDoc.getLong("audioDone"));

                    TotalManager.loadTotals(Profile.this, totals -> {
                        drawProgressUI(progress, totals);
                    });
                });
    }

    private void drawProgressUI(Map<String, Long> progress, Map<String, Integer> totals) {

        int hiraganaDone = progress.get("hiraganaDone").intValue();
        int katakanaDone = progress.get("katakanaDone").intValue();
        int kanjiDone = progress.get("kanjiDone").intValue();
        int grammarDone = progress.get("grammarDone").intValue();
        int textsDone = progress.get("textsDone").intValue();
        int audioDone = progress.get("audioDone").intValue();

        int totalHiragana = totals.get("hiraganaTotal");
        int totalKatakana = totals.get("katakanaTotal");
        int totalKanji = totals.get("kanjiTotal");
        int totalGrammar = totals.get("grammarTotal");
        int totalTexts = totals.get("textsTotal");
        int totalAudio = totals.get("audioTotal");

        int sumDone = hiraganaDone + katakanaDone + kanjiDone + grammarDone + textsDone + audioDone;
        int sumTotal = totalHiragana + totalKatakana + totalKanji + totalGrammar + totalTexts + totalAudio;

        int percent = (int) ((sumDone * 100f) / sumTotal);



        animateProgress(
                findViewById(R.id.hiraganaBar),
                (int) (hiraganaDone * 100f / totalHiragana)
        );

        animateProgress(
                findViewById(R.id.katakanaBar),
                (int) (katakanaDone * 100f / totalKatakana)
        );

        animateProgress(
                findViewById(R.id.kanjiBar),
                (int) (kanjiDone * 100f / totalKanji)
        );

        animateProgress(
                findViewById(R.id.grammarBar),
                (int) (grammarDone * 100f / totalGrammar)
        );

        animateProgress(
                findViewById(R.id.textsBar),
                (int) (textsDone * 100f / totalTexts)
        );

        animateProgress(
                findViewById(R.id.audioBar),
                (int) (audioDone * 100f / totalAudio)
        );


        ((TextView)findViewById(R.id.hiraganaLabel))
                .setText("Хирагана " + hiraganaDone + " / " + totalHiragana);

        ((TextView)findViewById(R.id.katakanaLabel))
                .setText("Катакана " + katakanaDone + " / " + totalKatakana);

        ((TextView)findViewById(R.id.kanjiLabel))
                .setText("Кандзи " + kanjiDone + " / " + totalKanji);

        ((TextView)findViewById(R.id.grammarLabel))
                .setText("Грамматика " + grammarDone + " / " + totalGrammar);

        ((TextView)findViewById(R.id.textsLabel))
                .setText("Тексты " + textsDone + " / " + totalTexts);

        ((TextView)findViewById(R.id.audioLabel))
                .setText("Аудио " + audioDone + " / " + totalAudio);


        ProgressRingsView ringsView = findViewById(R.id.ringsView);

        float[] ringProgress = new float[]{
                hiraganaDone * 100f / totalHiragana,
                katakanaDone * 100f / totalKatakana,
                kanjiDone * 100f / totalKanji,
                grammarDone * 100f / totalGrammar,
                textsDone * 100f / totalTexts,
                audioDone * 100f / totalAudio
        };

        ringsView.setProgress(ringProgress);

    }

    private void animateProgress(
            LinearProgressIndicator bar,
            int targetPercent
    ) {
        bar.setProgress(0);

        android.animation.ValueAnimator animator =
                android.animation.ValueAnimator.ofInt(0, targetPercent);

        animator.setDuration(700);
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator());

        animator.addUpdateListener(animation ->
                bar.setProgress((int) animation.getAnimatedValue())
        );

        animator.start();
    }


}