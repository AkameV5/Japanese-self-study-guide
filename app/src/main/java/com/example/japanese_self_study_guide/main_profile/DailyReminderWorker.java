package com.example.japanese_self_study_guide.main_profile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.japanese_self_study_guide.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DailyReminderWorker extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Daily").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    List<?> list = (List<?>) doc.get("recommendations");
                    if (list == null || list.isEmpty()) return;

                    showNotification(context);
                });
    }

    private void showNotification(Context context) {
        String channelId = "daily_reminder";

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Ежедневные задания",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            nm.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Ежедневные задания ждут 👀")
                .setContentText("Ты ещё не выполнил задания на сегодня")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        nm.notify(1001, notification);
    }
}
