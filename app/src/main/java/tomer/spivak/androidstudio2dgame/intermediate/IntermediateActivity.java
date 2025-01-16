package tomer.spivak.androidstudio2dgame.intermediate;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.game.GameActivity;
import tomer.spivak.androidstudio2dgame.home.HomeFragment;
import tomer.spivak.androidstudio2dgame.home.LoginFragment;
import tomer.spivak.androidstudio2dgame.home.SignUpFragment;

public class IntermediateActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    //private FrameLayout flMain;
    MaterialToolbar tbMain;
    Context context;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intermediate);


        init();
        context = this;

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_about) {
                    replaceFragment(new AboutFragment());
                } else if (id == R.id.nav_Rules) {
                    replaceFragment(new RulesFragment());
                } else if (id == R.id.go_to_game){
                    Intent intent = new Intent(context, GameActivity.class);
                    startActivity(intent);
                }
                drawerLayout.closeDrawer(navigationView);
                return true;
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

    }



    private void init() {
        initDrawerToolBar();
        initHeader();
        replaceFragment(new RulesFragment());

    }

    private void initHeader() {
        TextView tvUsername = navigationView.getHeaderView(0).
                findViewById(R.id.header_username);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("TAG", "initHeader: " + currentUser.getDisplayName());
        Log.d("TAG", "initHeader: " + currentUser.getEmail());
        tvUsername.setText(currentUser.getDisplayName());

        //tvUsername.setText();
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
        transaction.replace(R.id.flIntermediate, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}