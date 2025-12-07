package com.example.japanese_self_study_guide.audio;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.japanese_self_study_guide.R;

public class AudioPlayerActivity extends AppCompatActivity {

    private MediaPlayer player;
    private String url;

    private Button playPauseBtn;
    private Button btn05, btn1, btn2;
    private Button btnExercises;

    private SeekBar seekBar;
    private Handler handler = new Handler();

    private boolean isPrepared = false;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        url = getIntent().getStringExtra("audio_url");
        String name = getIntent().getStringExtra("audio_name");
        String description = getIntent().getStringExtra("audio_description");
        int audioId = getIntent().getIntExtra("audioId", -1);
        Log.d("AUDIO_DEBUG", "Received audioId = " + audioId);

        seekBar = findViewById(R.id.seekBar);

        TextView moduleTitle = findViewById(R.id.moduleTitle);
        TextView audioTitle = findViewById(R.id.audioTitle);

        moduleTitle.setText(name);
        audioTitle.setText(description);

        playPauseBtn = findViewById(R.id.playPauseButton);
        btn05 = findViewById(R.id.speed05);
        btn1 = findViewById(R.id.speed1);
        btn2 = findViewById(R.id.speed2);
        btnExercises = findViewById(R.id.btnExercises);

        initPlayer();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isPrepared) {
                    player.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

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
                updateSeekBar(); // важно
            }
        });

        btn05.setOnClickListener(v -> changeSpeed(0.5f));
        btn1.setOnClickListener(v -> changeSpeed(1.0f));
        btn2.setOnClickListener(v -> changeSpeed(2.0f));

        btnExercises.setVisibility(View.GONE);

        btnExercises.setOnClickListener(v -> {
            Intent intent = new Intent(this, AudioExerciseActivity.class);
            intent.putExtra("audioId", audioId);
            startActivity(intent);
        });

    }

    private void initPlayer() {
        player = new MediaPlayer();

        try {
            player.setDataSource(url);

            player.setOnPreparedListener(mp -> {
                isPrepared = true;

                // Инициализация seekBar
                seekBar.setMax(player.getDuration());
                updateSeekBar();

                // Если пользователь нажмёт play
                isPlaying = false;
            });

            player.setOnCompletionListener(mp -> {
                isPlaying = false;
                playPauseBtn.setText("Воспроизвести");
                btnExercises.setVisibility(View.VISIBLE);
            });

            player.prepareAsync();

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
                player.pause();
            }
        }
    }

    private void updateSeekBar() {
        if (player != null && isPlaying) {
            seekBar.setProgress(player.getCurrentPosition());
            handler.postDelayed(this::updateSeekBar, 300);
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
