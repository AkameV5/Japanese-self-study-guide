package com.example.japanese_self_study_guide.main_profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FirestoreProfilePhoto {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static Task<Void> savePhoto(String userId, Uri imageUri, Context context) {
        try {
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();

            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 500, 500, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            Map<String, Object> updates = new HashMap<>();
            updates.put("profilePhoto", base64Image);

            return db.collection("Users").document(userId).update(updates);

        } catch (Exception e) {
            e.printStackTrace();
            return com.google.android.gms.tasks.Tasks.forException(e);
        }
    }

    public static void loadPhoto(String userId, ImageView imageView, int defaultResId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("profilePhoto")) {
                        String base64Image = doc.getString("profilePhoto");
                        if (base64Image != null && !base64Image.isEmpty()) {
                            try {
                                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                imageView.setImageBitmap(bitmap);
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    imageView.setImageResource(defaultResId);
                })
                .addOnFailureListener(e -> {
                    imageView.setImageResource(defaultResId);
                });
    }

    public static Task<Void> deletePhoto(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("profilePhoto", com.google.firebase.firestore.FieldValue.delete());
        return db.collection("Users").document(userId).update(updates);
    }
}