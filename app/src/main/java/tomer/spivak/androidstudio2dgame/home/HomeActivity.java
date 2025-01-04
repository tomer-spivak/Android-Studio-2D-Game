package tomer.spivak.androidstudio2dgame.home;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.window.OnBackInvokedCallback;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import tomer.spivak.androidstudio2dgame.R;

public class HomeActivity extends AppCompatActivity{

    private DrawerLayout drawerLayout;
    //private FrameLayout flMain;
    MaterialToolbar tbMain;

    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);


        init();

        replaceFragment(new HomeFragment());
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    replaceFragment(new HomeFragment());
                } else if (id == R.id.nav_about) {
                    replaceFragment(new AboutFragment());
                }
                else if (id == R.id.nav_Login) {
                    replaceFragment(new LoginFragment());

                }
                else if (id == R.id.nav_SignUp) {
                    replaceFragment(new SignUpFragment());
                } else if (id == R.id.nav_Rules) {
                    replaceFragment(new RulesFragment());
                }
                drawerLayout.closeDrawer(navigationView);
                return true;
            }
        });
        navigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("debug", "rizz");
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            OnBackInvokedCallback callback = new OnBackInvokedCallback() {
                @Override
                public void onBackInvoked() {
                    if (drawerLayout.isDrawerOpen(navigationView)) {
                        drawerLayout.closeDrawer(navigationView);
                    } else
                        finish();
                }
            };
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    0, callback);
        }
        Log.d("debug", String.valueOf(navigationView.getMenu().getItem(0).getItemId() == R.id.nav_home));

    }



    private void init() {
        //flMain = findViewById(R.id.flMain);
        initDrawerToolBar();

    }

    private void initDrawerToolBar() {
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        tbMain = findViewById(R.id.tbMain);
        setSupportActionBar(tbMain);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                tbMain, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        //toggle.setDrawerIndicatorEnabled(false); // Disable default icon

        toggle.syncState();

        //toggle.setHomeAsUpIndicator(R.drawable.ic_launcher_foreground); // Set custom icon

        tbMain.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(navigationView)){
                    drawerLayout.closeDrawer(navigationView);
                }
                else {
                    drawerLayout.openDrawer(navigationView);
                }
            }
        });

        drawerLayout.addDrawerListener(toggle);

    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flMain, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


}