package tomer.spivak.androidstudio2dgame.graphics;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.logic.LeaderboardEntry;
import tomer.spivak.androidstudio2dgame.projectManagement.DatabaseRepository;

public class LeaderboardFragment extends Fragment {
    private LeaderboardAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        RecyclerView rv = view.findViewById(R.id.rvLeaderboard);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        EditText searchBar = view.findViewById(R.id.searchBar);

        searchBar.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        Spinner spinnerSort = view.findViewById(R.id.spinnerSort);
        String[] options = {"Max Round", "Games Played", "Enemies Defeated", "Victories"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, android.R.id.text1, options);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerAdapter);

        int victoriesIndex = options.length - 1;
        spinnerSort.setSelection(victoriesIndex, false);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) { }

            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                switch (pos) {
                    case 0:
                        adapter.sortByMaxRound();
                        break;
                    case 1:
                        adapter.sortByGamesPlayed();
                        break;
                    case 2:
                        adapter.sortByEnemiesDefeated();
                        break;
                    case 3:
                    default:
                        adapter.sortByVictories();
                        break;
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new DatabaseRepository(requireContext()).fetchLeaderboardFromDatabase(
                        new OnSuccessListener<List<LeaderboardEntry>>() {
                            @Override
                            public void onSuccess(List<LeaderboardEntry> entries) {
                                adapter.updateData(entries);
                            }
                        }, requireContext());
    }
}
