package tomer.spivak.androidstudio2dgame.graphics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.logic.LeaderboardEntry;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private static final int SORT_BY_VICTORIES = 0;
    private static final int SORT_BY_GAMES_PLAYED = 1;
    private static final int SORT_BY_MAX_ROUND = 2;
    private static final int SORT_BY_ENEMIES_DEFEATED = 3;

    private final List<LeaderboardEntry> fullList = new ArrayList<>();
    private final List<LeaderboardEntry> filteredList = new ArrayList<>();
    private String lastQuery = "";
    private int currentSortField = SORT_BY_VICTORIES;

    public LeaderboardAdapter(List<LeaderboardEntry> data) {
        updateData(data);
    }

    public void updateData(List<LeaderboardEntry> data) {
        fullList.clear();
        fullList.addAll(data);
        sortByVictories();
    }

    public void sortByVictories() {
        currentSortField = SORT_BY_VICTORIES;
        applySortAndFilter();
    }

    public void sortByGamesPlayed() {
        currentSortField = SORT_BY_GAMES_PLAYED;
        applySortAndFilter();
    }

    public void sortByMaxRound() {
        currentSortField = SORT_BY_MAX_ROUND;
        applySortAndFilter();
    }

    public void sortByEnemiesDefeated() {
        currentSortField = SORT_BY_ENEMIES_DEFEATED;
        applySortAndFilter();
    }

    public void filter(String query) {
        lastQuery = (query == null) ? "" : query.trim().toLowerCase();
        applySortAndFilter();
    }

    private void applySortAndFilter() {
        filteredList.clear();
        for (LeaderboardEntry e : fullList) {
            if (lastQuery.isEmpty() || e.getDisplayName().toLowerCase().contains(lastQuery)) {
                insertSorted(e);
            }
        }
        notifyDataSetChanged();
    }

    private void insertSorted(LeaderboardEntry entry) {
        int position = 0;
        while (position < filteredList.size() && !shouldInsertBefore(entry, filteredList.get(position))) {
            position++;
        }
        filteredList.add(position, entry);
    }

    private boolean shouldInsertBefore(LeaderboardEntry a, LeaderboardEntry b) {
        switch (currentSortField) {
            case SORT_BY_GAMES_PLAYED:
                return a.getGamesPlayed() > b.getGamesPlayed();
            case SORT_BY_MAX_ROUND:
                return a.getMaxRound() > b.getMaxRound();
            case SORT_BY_ENEMIES_DEFEATED:
                return a.getEnemiesDefeated() > b.getEnemiesDefeated();
            case SORT_BY_VICTORIES:
            default:
                return a.getVictories() > b.getVictories();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        LeaderboardEntry e = filteredList.get(pos);
        h.rank.setText((pos + 1) + ".");
        h.user.setText(e.getDisplayName());
        h.max.setText("🏆 Max Round: " + e.getMaxRound());
        h.games.setText("🎮 Games: " + e.getGamesPlayed());
        h.kills.setText("💀 Defeated: " + e.getEnemiesDefeated());
        h.wins.setText("🏅 Victories: " + e.getVictories());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank, user, games, kills, max, wins;

        ViewHolder(View v) {
            super(v);
            rank = v.findViewById(R.id.tvRank);
            user = v.findViewById(R.id.usernameTextView);
            games = v.findViewById(R.id.tvGamesPlayed);
            kills = v.findViewById(R.id.tvEnemiesDefeated);
            max = v.findViewById(R.id.tvMaxRound);
            wins = v.findViewById(R.id.tvVictories);
        }
    }
}
