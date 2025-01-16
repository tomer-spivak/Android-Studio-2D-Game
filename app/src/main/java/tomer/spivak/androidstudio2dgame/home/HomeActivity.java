package tomer.spivak.androidstudio2dgame.home;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import tomer.spivak.androidstudio2dgame.R;

public class HomeActivity extends AppCompatActivity{
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        init();

        replaceFragment(new HomeFragment());
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Log.d("debug", "onNavigationItemReselected: " + id);
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
    }
    void init(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }
    private void replaceFragment(Fragment fragment) {
        Log.d("debug", "replaceFragment: " + fragment);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flHome, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}