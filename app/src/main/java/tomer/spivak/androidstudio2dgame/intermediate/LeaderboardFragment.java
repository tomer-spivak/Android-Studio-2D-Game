package tomer.spivak.androidstudio2dgame.intermediate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameActivity.FirebaseRepository;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class LeaderboardFragment extends Fragment {
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private final ArrayList<LeaderboardEntry> leaderboardList = new ArrayList<>();
    FirebaseRepository firebaseRep = new FirebaseRepository(getContext());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        recyclerView = view.findViewById(R.id.rvLeaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(leaderboardList);
        recyclerView.setAdapter(adapter);

        fetchLeaderboardData();
        return view;
    }

    private void fetchLeaderboardData() {
        firebaseRep.fetchLeaderboardFromDatabase(new LeaderboardCallback() {
            @Override
            public void onLeaderboardFetched(List<LeaderboardEntry> leaderboardEntries) {
                leaderboardList.clear();
                leaderboardList.addAll(leaderboardEntries);
                adapter.notifyDataSetChanged();
            }
        });
    }}

