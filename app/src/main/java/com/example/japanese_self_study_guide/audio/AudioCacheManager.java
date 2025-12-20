package com.example.japanese_self_study_guide.audio;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AudioCacheManager {

    public interface Callback {
        void onReady(File audioFile);
        void onError();
    }

    public static void preload(
            Context context,
            int audioId,
            String url,
            Callback callback
    ) {
        File file = new File(context.getCacheDir(), "audio_" + audioId + ".mp3");

        if (file.exists()) {
            callback.onReady(file);
            return;
        }

        new Thread(() -> {
            try {
                URL audioUrl = new URL(url);
                HttpURLConnection connection =
                        (HttpURLConnection) audioUrl.openConnection();

                connection.connect();

                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(file)) {

                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }

                callback.onReady(file);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError();
            }
        }).start();
    }
}
