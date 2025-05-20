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
import tomer.spivak.androidstudio2dgame.graphics.HomeActivity;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int hour = intent.getIntExtra("extra_hour", 0);
        Intent tapIntent = new Intent(context, HomeActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, hour * 100, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reminder_channel").setSmallIcon(R.drawable.logo).setContentTitle("TowerLands")
                .setContentText("Continue playing!").setAutoCancel(true).setContentIntent(contentIntent).setPriority(NotificationCompat.PRIORITY_HIGH);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(hour * 100, builder.build());
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent nextNotification = new Intent(context, NotificationReceiver.class);
        nextNotification.putExtra("extra_hour", hour);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, hour * 100, nextNotification, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pendingIntent);
        }
    }

    public static void createChannel(Context context) {
        (context.getSystemService(NotificationManager.class)).createNotificationChannel(new NotificationChannel(
                "reminder_channel", "Game Reminders", NotificationManager.IMPORTANCE_HIGH));
    }
}

