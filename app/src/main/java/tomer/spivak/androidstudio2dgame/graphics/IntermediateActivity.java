package tomer.spivak.androidstudio2dgame.graphics;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;


import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.projectManagement.DatabaseRepository;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;

public class IntermediateActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Context context;
    private NavigationView navigationView;
    private DatabaseRepository databaseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intermediate);

        context = this;
        databaseRepository = DatabaseRepository.getInstance(context);
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        MaterialToolbar tbMain = findViewById(R.id.tbMain);
        setSupportActionBar(tbMain);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, tbMain, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

        TextView tvUsername = navigationView.getHeaderView(0).findViewById(R.id.header_username);
        ImageView ivProfile = navigationView.getHeaderView(0).findViewById(R.id.header_image);

        databaseRepository.reloadUser(tvUsername, ivProfile, context);


        replaceFragment(new RulesFragment(), false);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_about && !(getSupportFragmentManager().findFragmentById(R.id.flIntermediate) instanceof AboutFragment)) {
                    replaceFragment(new AboutFragment(), true);
                } else if (id == R.id.nav_Rules && !(getSupportFragmentManager().findFragmentById(R.id.flIntermediate) instanceof RulesFragment)) {
                    replaceFragment(new RulesFragment(), true);
                } else if (id == R.id.nav_leaderboard && !(getSupportFragmentManager().findFragmentById(R.id.flIntermediate) instanceof LeaderboardFragment)) {
                    replaceFragment(new LeaderboardFragment(), true);
                }
                else if (id == R.id.go_to_game){
                    Intent intent = getIntent();
                    boolean isGuest = intent.getBooleanExtra("guest", false);
                    if (isGuest) {
                        Intent intentGame = new Intent(context, GameActivity.class);
                        intentGame.putExtra("isContinue", false);
                        createNewGame();
                        drawerLayout.closeDrawers();
                        return true;
                    }
                    databaseRepository.checkIfTheresAGame(new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean gameExists) {
                            if (gameExists) {
                                new AlertDialog.Builder(context).setTitle("continue current game or start new one?")
                                        .setPositiveButton("continue", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(context, GameActivity.class);
                                        intent.putExtra("isContinue", true);
                                        intent.putExtra("difficultyLevel", DifficultyLevel.MEDIUM);
                                        startActivity(intent);
                                    }
                                }).setNegativeButton("new game", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        createNewGame();
                                    }
                                }).create().show();
                            } else {
                                createNewGame();
                                drawerLayout.closeDrawers();
                            }
                        }
                    }, context);
                }
                drawerLayout.closeDrawer(navigationView);
                return true;
            }

            private void createNewGame() {
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.alert_dialog_difficulty, null);

                RadioGroup group = dialogView.findViewById(R.id.difficultyGroup);
                Button cancelButton = dialogView.findViewById(R.id.cancelButton);
                Button continueButton = dialogView.findViewById(R.id.continueButton);

                AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView).create();

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                continueButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                    }
                });
                dialog.show();
            }
        });
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