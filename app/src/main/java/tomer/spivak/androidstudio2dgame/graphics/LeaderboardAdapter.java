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

    private enum SortBy { VICTORIES, GAMES_PLAYED, MAX_ROUND, ENEMIES_DEFEATED }

    private final List<LeaderboardEntry> fullList     = new ArrayList<>();
    private final List<LeaderboardEntry> filteredList = new ArrayList<>();
    private String lastQuery = "";
    private SortBy currentSort = SortBy.VICTORIES;

    public LeaderboardAdapter(List<LeaderboardEntry> data) {
        updateData(data);
    }

    public void updateData(List<LeaderboardEntry> data) {
        fullList.clear();
        fullList.addAll(data);
        sortByVictories();
    }

    public void sortByVictories() {
        sort(SortBy.VICTORIES);
    }
    public void sortByGamesPlayed() {
        sort(SortBy.GAMES_PLAYED);
    }
    public void sortByMaxRound() {
        sort(SortBy.MAX_ROUND);
    }
    public void sortByEnemiesDefeated() {
        sort(SortBy.ENEMIES_DEFEATED);
    }

    private void sort(SortBy field) {
        currentSort = field;
        for (int i = 0; i < fullList.size(); i++) {
            for (int j = 0; j < fullList.size() - 1; j++) {
                LeaderboardEntry a = fullList.get(j);
                LeaderboardEntry b = fullList.get(j + 1);
                if (shouldSwap(a, b)) {
                    fullList.set(j, b);
                    fullList.set(j + 1, a);
                }
            }
        }
        applyFilter();
    }

    private boolean shouldSwap(LeaderboardEntry a, LeaderboardEntry b) {
        switch (currentSort) {
            case GAMES_PLAYED:
                return a.getGamesPlayed() < b.getGamesPlayed();
            case MAX_ROUND:
                return a.getMaxRound() < b.getMaxRound();
            case ENEMIES_DEFEATED:
                return a.getEnemiesDefeated() < b.getEnemiesDefeated();
            case VICTORIES:
            default:
                return a.getVictories() < b.getVictories();
        }
    }

    public void filter(String query) {
        if (query == null) {
            lastQuery = "";
        } else {
            lastQuery = query.trim().toLowerCase();
        }
        applyFilter();
    }

    private void applyFilter() {
        filteredList.clear();
        if (lastQuery.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            for (LeaderboardEntry e : fullList) {
                if (e.getDisplayName().toLowerCase().contains(lastQuery)) {
                    filteredList.add(e);
                }
            }
        }
        notifyDataSetChanged();
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
        h.rank .setText((pos + 1) + ".");
        h.user .setText(e.getDisplayName());
        h.max  .setText("🏆 Max Round: "    + e.getMaxRound());
        h.games.setText("🎮 Games: "        + e.getGamesPlayed());
        h.kills.setText("💀 Defeated: "     + e.getEnemiesDefeated());
        h.wins .setText("🏅 Victories: "    + e.getVictories());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank, user, games, kills, max, wins;
        ViewHolder(View v) {
            super(v);
            rank  = v.findViewById(R.id.tvRank);
            user  = v.findViewById(R.id.usernameTextView);
            games = v.findViewById(R.id.tvGamesPlayed);
            kills = v.findViewById(R.id.tvEnemiesDefeated);
            max   = v.findViewById(R.id.tvMaxRound);
            wins  = v.findViewById(R.id.tvVictories);
        }
    }
}
