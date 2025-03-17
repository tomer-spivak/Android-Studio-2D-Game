package tomer.spivak.androidstudio2dgame.intermediate;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameActivity.FirebaseRepository;
import tomer.spivak.androidstudio2dgame.gameActivity.GameActivity;
import tomer.spivak.androidstudio2dgame.gameActivity.GameCheckCallback;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;

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
                    replaceFragment(new AboutFragment(), true);
                } else if (id == R.id.nav_Rules) {
                    replaceFragment(new RulesFragment(), true);
                } else if (id == R.id.nav_leaderboard) {
                    replaceFragment(new LeaderboardFragment(), true);
                }
                else if (id == R.id.go_to_game){
                    FirebaseRepository firebaseRepository = new FirebaseRepository(context);
                    firebaseRepository.checkIfTheresAGame(new GameCheckCallback() {
                        @Override
                        public void onCheckCompleted(boolean gameExists) {
                            if (gameExists) {
                                AlertDialog dialog = continueOrStartNewGame();
                                dialog.show();
                            } else {
                                createNewGame();
                            }
                        }
                    });
                }
                drawerLayout.closeDrawer(navigationView);
                return true;
            }


            @NonNull
            private AlertDialog continueOrStartNewGame() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("continue current game or start new one?");
                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, GameActivity.class);
                        intent.putExtra("isContinue", true);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("new game", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createNewGame();
                    }
                });
                return builder.create();
            }

            private void createNewGame() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final DifficultyLevel[] difficultyLevel = {DifficultyLevel.EASY};
                builder.setTitle("select difficulty level")
                        .setItems(new String[]{"Easy", "Normal", "Hard"},new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        difficultyLevel[0] = DifficultyLevel.EASY;
                                        break;
                                    case 1:
                                        difficultyLevel[0] = DifficultyLevel.MEDIUM;
                                        // Handle Option 2
                                        break;
                                    case 2:
                                        difficultyLevel[0] = DifficultyLevel.HARD;
                                        // Handle Option 3
                                        break;
                                }
                                Intent intent = new Intent(context, GameActivity.class);
                                intent.putExtra("difficultyLevel", difficultyLevel[0].name());
                                intent.putExtra("isContinue", false);
                                startActivity(intent);
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean o) {

                    }
                }
        );

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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

    }



    private void init() {
        initDrawerToolBar();
        initHeader();
        replaceFragment(new RulesFragment(), false);

    }

    private void initHeader() {
        TextView tvUsername = navigationView.getHeaderView(0).
                findViewById(R.id.header_username);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            tvUsername.setText("Guest");
            return;
        }
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

    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flIntermediate, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}