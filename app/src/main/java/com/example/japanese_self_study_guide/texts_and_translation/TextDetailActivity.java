package com.example.japanese_self_study_guide.texts_and_translation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.japanese_self_study_guide.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class TextDetailActivity extends AppCompatActivity {

    private TextView titleView, textContent;
    private MaterialButtonToggleGroup toggleLanguage;
    private MaterialButton btnPrev, btnNext;

    private int textId;
    private List<String> japaneseParagraphs = new ArrayList<>();
    private List<String> translationParagraphs = new ArrayList<>();
    private int currentIndex = 0;
    private MaterialButton btnExercises;
    private String japaneseTitle = "";
    private String translationTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_detail);

        titleView = findViewById(R.id.textTitle);
        textContent = findViewById(R.id.textContent);
        toggleLanguage = findViewById(R.id.toggleLanguage);
        btnPrev = findViewById(R.id.buttonPrev);
        btnNext = findViewById(R.id.buttonNext);
        btnExercises = findViewById(R.id.buttonExercises);
        btnExercises.setOnClickListener(v -> openExercises());

        textId = getIntent().getIntExtra("textId", -1);

        toggleLanguage.check(R.id.buttonJapanese);

        if (textId != -1) {
            loadText();
            loadTranslation();
        }

        toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) updateDisplayedParagraph();
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateDisplayedParagraph();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < getCurrentParagraphs().size() - 1) {
                currentIndex++;
                updateDisplayedParagraph();
            }
        });
    }

    private void loadText() {
        FirebaseFirestore.getInstance().collection("Texts")
                .whereEqualTo("id", textId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        var doc = query.getDocuments().get(0);
                        japaneseTitle = doc.getString("title");
                        titleView.setText(japaneseTitle);

                        List<String> sentences = (List<String>) doc.get("sentences");
                        japaneseParagraphs = groupIntoParagraphs(sentences);

                        updateDisplayedParagraph();
                    }
                })
                .addOnFailureListener(e -> Log.e("TextDetail", "Ошибка загрузки оригинала", e));
    }

    private void loadTranslation() {
        FirebaseFirestore.getInstance().collection("Translations_texts")
                .whereEqualTo("textId", textId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        var doc = query.getDocuments().get(0);

                        translationTitle = doc.getString("translationTitle");
                        Log.d("TextDetail", "translationTitle = " + translationTitle);

                        List<String> sentences = (List<String>) doc.get("sentences");
                        translationParagraphs = groupIntoParagraphs(sentences);

                        // Если уже выбран режим перевода — сразу обновим UI
                        if (toggleLanguage.getCheckedButtonId() == R.id.buttonTranslation) {
                            updateDisplayedParagraph();
                        }
                    } else {
                        Log.w("TextDetail", "Документ перевода не найден для textId=" + textId);
                    }
                })
                .addOnFailureListener(e -> Log.e("TextDetail", "Ошибка загрузки перевода", e));
    }

    private List<String> groupIntoParagraphs(List<String> sentences) {
        List<String> paragraphs = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (String s : sentences) {
            if (s.trim().isEmpty()) {
                if (builder.length() > 0) {
                    paragraphs.add(builder.toString().trim());
                    builder.setLength(0);
                }
            } else {
                builder.append(s).append("\n");
            }
        }

        if (builder.length() > 0) {
            paragraphs.add(builder.toString().trim());
        }

        return paragraphs;
    }

    private void updateDisplayedParagraph() {
        List<String> currentList = getCurrentParagraphs();
        if (currentList == null || currentList.isEmpty()) return;

        String currentText = currentList.get(currentIndex);
        textContent.setText(currentText);

        int checkedId = toggleLanguage.getCheckedButtonId();
        if (checkedId == R.id.buttonJapanese) {
            titleView.setText(japaneseTitle);
        } else if (checkedId == R.id.buttonTranslation) {
            titleView.setText(
                    translationTitle != null && !translationTitle.isEmpty()
                            ? translationTitle
                            : "Перевод"
            );
        }

        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < currentList.size() - 1);

        btnExercises.setVisibility(
                currentIndex == getCurrentParagraphs().size() - 1
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private List<String> getCurrentParagraphs() {
        return toggleLanguage.getCheckedButtonId() == R.id.buttonTranslation
                ? translationParagraphs
                : japaneseParagraphs;
    }

    private void openExercises() {
        Intent intent = new Intent(this, TextExercisesActivity.class);
        intent.putExtra("textId", textId);
        startActivity(intent);
    }

}
