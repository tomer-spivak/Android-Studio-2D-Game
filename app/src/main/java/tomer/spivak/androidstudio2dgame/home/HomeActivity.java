package tomer.spivak.androidstudio2dgame.home;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.Calendar;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.projectManagement.NotificationReceiver;
import tomer.spivak.androidstudio2dgame.projectManagement.DatabaseRepository;

public class HomeActivity extends AppCompatActivity{
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        init();

        DatabaseRepository.getInstance(this).signOut(this);

        replaceFragment(new HomeFragment());
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    replaceFragment(new HomeFragment());
                } else if (id == R.id.nav_Login) {
                    replaceFragment(new LoginFragment());
                } else if (id == R.id.nav_SignUp) {
                    replaceFragment(new SignUpFragment());
                }
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager1 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager1.canScheduleExactAlarms()) {
                Intent intent1 = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent1.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent1);
            }
        }
        scheduleNotification(this, 10);  // 10:00 AM
        scheduleNotification(this, 16);  // 4:00 PM

        scheduleNotification(this, 22);  // 10:00 PM

    }


    private void scheduleNotification(Context context, int hour) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);


        int requestCode = hour * 100;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);


        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }





        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


    void init(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flHome, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}