package tomer.spivak.androidstudio2dgame.intermediate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.helper.DatabaseRepository;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.internal.TextWatcherAdapter;

import java.util.ArrayList;
import java.util.List;


public class LeaderboardFragment extends Fragment {
    private LeaderboardAdapter adapter;
    private final ArrayList<LeaderboardEntry> leaderboardList = new ArrayList<>();
    DatabaseRepository databaseRepository = DatabaseRepository.getInstance(getContext());


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        // RecyclerView
        RecyclerView rv = view.findViewById(R.id.rvLeaderboard);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        // Search bar
        EditText searchBar = view.findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
        });

        // 1) Set up Spinner with a custom item layout
        Spinner spinner = view.findViewById(R.id.spinnerSort);
        String[] options = getResources().getStringArray(R.array.leaderboard_sort_options);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item,      // your custom “closed” view (TextView @android:id/text1)
                android.R.id.text1,           // or android.R.id.text1 if you switched IDs
                options
        );

// use your brand-new dropdown layout:
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(spinnerAdapter);



        // 2) Listen for selections
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        adapter.sortBy(LeaderboardAdapter.SortType.GAMES_PLAYED);
                        break;
                    case 2:
                        adapter.sortBy(LeaderboardAdapter.SortType.ENEMIES_DEFEATED);
                        break;
                    default:
                        adapter.sortBy(LeaderboardAdapter.SortType.MAX_ROUND);
                }
            }
        });

        // Fetch and display data
        DatabaseRepository.getInstance(requireContext())
                .fetchLeaderboardFromDatabase(entries -> adapter.updateData(entries));

        return view;
    }

    private void fetchLeaderboardData() {
        databaseRepository.fetchLeaderboardFromDatabase(new LeaderboardCallback() {
            @Override
            public void onLeaderboardFetched(List<LeaderboardEntry> leaderboardEntries) {
                leaderboardList.clear();
                leaderboardList.addAll(leaderboardEntries);
                adapter.notifyDataSetChanged();
            }
        });
    }}

