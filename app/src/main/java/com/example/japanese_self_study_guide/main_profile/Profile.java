package com.example.japanese_self_study_guide.main_profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;
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
}
