package com.example.japanese_self_study_guide.audio;

import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;

public class AudioPlayerActivity extends AppCompatActivity {

    private MediaPlayer player;
    private String url;

    private Button playPauseBtn;
    private Button btn05, btn1, btn2;

    private boolean isPrepared = false;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        url = getIntent().getStringExtra("audio_url");
        String name = getIntent().getStringExtra("audio_name");
        String description = getIntent().getStringExtra("audio_description");

        TextView moduleTitle = findViewById(R.id.moduleTitle);
        TextView audioTitle = findViewById(R.id.audioTitle);

        moduleTitle.setText(name);
        audioTitle.setText(description);

        playPauseBtn = findViewById(R.id.playPauseButton);

        btn05 = findViewById(R.id.speed05);
        btn1 = findViewById(R.id.speed1);
        btn2 = findViewById(R.id.speed2);

        initPlayer();

        // Play / pause button
        playPauseBtn.setOnClickListener(v -> {
            if (!isPrepared) return;

            if (isPlaying) {
                player.pause();
                isPlaying = false;
                playPauseBtn.setText("Воспроизвести");
            } else {
                player.start();
                isPlaying = true;
                playPauseBtn.setText("Пауза");
            }
        });

        // Speed controls
        btn05.setOnClickListener(v -> changeSpeed(0.5f));
        btn1.setOnClickListener(v -> changeSpeed(1.0f));
        btn2.setOnClickListener(v -> changeSpeed(2.0f));
    }

    private void initPlayer() {
        player = new MediaPlayer();
        try {
            player.setDataSource(url);
            player.prepareAsync();

            player.setOnPreparedListener(mp -> {
                isPrepared = true;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && player != null && isPrepared) {
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(speed);
            if (player.isPlaying()) {
                player.setPlaybackParams(params);
            } else {
                player.setPlaybackParams(params);
                player.pause(); // фикс бага: не запускаем автоматически
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
