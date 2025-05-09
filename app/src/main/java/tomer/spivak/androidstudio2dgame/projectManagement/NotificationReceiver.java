package tomer.spivak.androidstudio2dgame.projectManagement;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.home.HomeActivity;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_HOUR = "extra_hour";
    private static final String CHANNEL_ID = "reminder_channel";


    @Override
    public void onReceive(Context context, Intent intent) {
        int hour = intent.getIntExtra(EXTRA_HOUR, 0);
        showNotification(context, hour);
        scheduleNextReminder(context, hour);
    }

    public static void createChannel(Context ctx) {
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        nm.createNotificationChannel(
                new NotificationChannel(
                        CHANNEL_ID,
                        "Game Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                )
        );
    }

    private void showNotification(Context context, int hour) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent tapIntent = new Intent(context, HomeActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                hour * 100,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("TowerLands")
                .setContentText("Continue playing!")
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        nm.notify(hour * 100, builder.build());

    }
    private void scheduleNextReminder(Context context, int hour) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_HOUR, hour);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                hour * 100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    next.getTimeInMillis(),
                    pi
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    next.getTimeInMillis(),
                    pi
            );
        }
    }
}

