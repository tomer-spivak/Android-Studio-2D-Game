package tomer.spivak.androidstudio2dgame.intermediate;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;


import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.helper.DatabaseRepository;
import tomer.spivak.androidstudio2dgame.graphics.GameActivity;
import tomer.spivak.androidstudio2dgame.helper.GameCheckCallback;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;

public class IntermediateActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    MaterialToolbar tbMain;
    Context context;
    NavigationView navigationView;

    DatabaseRepository databaseRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intermediate);


        context = this;
        databaseRepository = DatabaseRepository.getInstance(context);
        init();
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
                    Intent intent = getIntent();
                    boolean isGuest = intent.getBooleanExtra("guest", false);
                    if (isGuest) {
                        Intent intent1 = new Intent(context, GameActivity.class);
                        intent1.putExtra("isContinue", false);
                        createNewGame();
                        drawerLayout.closeDrawers();
                        //startActivity(intent1);
                        return true;
                    }
                    if (DatabaseRepository.isOnline(context)){
                        databaseRepository.checkIfTheresAGame(new GameCheckCallback() {
                            @Override
                            public void onCheckCompleted(boolean gameExists) {
                                if (gameExists) {
                                    AlertDialog dialog = continueOrStartNewGame();
                                    dialog.show();
                                } else {
                                    createNewGame();
                                    drawerLayout.closeDrawers();
                                }
                            }
                        });
                    } else{
                        createNewGame();
                        drawerLayout.closeDrawers();
                    }

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
                        intent.putExtra("difficultyLevel", DifficultyLevel.MEDIUM);
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
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.alert_dialog_difficulty, null);

                RadioGroup group = dialogView.findViewById(R.id.difficultyGroup);
                Button cancelButton = dialogView.findViewById(R.id.cancelButton);
                Button continueButton = dialogView.findViewById(R.id.continueButton);

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setView(dialogView)
                        .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                cancelButton.setOnClickListener(v -> dialog.dismiss());

                continueButton.setOnClickListener(v -> {
                    DifficultyLevel selected = DifficultyLevel.EASY;
                    int checkedId = group.getCheckedRadioButtonId();
                    if (checkedId == R.id.normal) {
                        selected = DifficultyLevel.MEDIUM;
                    } else if (checkedId == R.id.hard) {
                        selected = DifficultyLevel.HARD;
                    }
                    Intent intent = new Intent(context, GameActivity.class);
                    intent.putExtra("difficultyLevel", selected.name());
                    intent.putExtra("isContinue", false);
                    dialog.dismiss();
                    context.startActivity(intent);

                });

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
        FirebaseUser user = databaseRepository.getUserInstance();
        if (user != null) {
            user.reload()
                    .addOnSuccessListener(aVoid -> actuallyPopulateHeader(user))
                    .addOnFailureListener(e -> actuallyPopulateHeader(user));  // even on failure, use whatever you have
        } else {
            actuallyPopulateHeader(null);
        }
    }

    private void actuallyPopulateHeader(@Nullable FirebaseUser user) {
        TextView tvUsername =
                navigationView.getHeaderView(0).findViewById(R.id.header_username);
        ImageView ivProfile =
                navigationView.getHeaderView(0).findViewById(R.id.header_image);

        String displayName = user != null && user.getDisplayName() != null ? user.getDisplayName() : "guest";
        tvUsername.setText(displayName);

        databaseRepository.fetchAndSetImage(ivProfile, this);
    }


    private void initDrawerToolBar() {
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        tbMain = findViewById(R.id.tbMain);
        setSupportActionBar(tbMain);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                tbMain, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);


        toggle.syncState();


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