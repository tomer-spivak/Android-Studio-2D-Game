package tomer.spivak.androidstudio2dgame.intermediate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tomer.spivak.androidstudio2dgame.R;

public class LeaderboardAdapter
        extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    private final List<LeaderboardEntry> fullList = new ArrayList<>();
    private final List<LeaderboardEntry> filteredList = new ArrayList<>();
    private String lastQuery = "";

    public LeaderboardAdapter(List<LeaderboardEntry> initialData) {
        updateData(initialData);
    }

    public void updateData(List<LeaderboardEntry> newData) {
        fullList.clear();
        fullList.addAll(newData);
        sortBy(SortType.MAX_ROUND);     // default initial sort
    }

    public enum SortType { MAX_ROUND, GAMES_PLAYED, ENEMIES_DEFEATED }

    /** Sort fullList, then re-filter by lastQuery */
    public void sortBy(SortType type) {
        Comparator<LeaderboardEntry> cmp;
        switch (type) {
            case GAMES_PLAYED:
                cmp = (a, b) -> Integer.compare(b.getGamesPlayed(), a.getGamesPlayed());
                break;
            case ENEMIES_DEFEATED:
                cmp = (a, b) -> Integer.compare(b.getEnemiesDefeated(), a.getEnemiesDefeated());
                break;
            case MAX_ROUND:
            default:
                cmp = (a, b) -> Integer.compare(b.getMaxRound(), a.getMaxRound());
                break;
        }
        fullList.sort(cmp);
        // re-apply filter
        filter(lastQuery);
    }

    public void filter(String query) {
        lastQuery = query == null ? "" : query.trim().toLowerCase();
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
    public LeaderboardViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LeaderboardViewHolder holder, int position
    ) {
        LeaderboardEntry entry = filteredList.get(position);
        holder.usernameTextView.setText(entry.getDisplayName());
        holder.maxRoundTextView.setText("🏆 Max Round: " + entry.getMaxRound());
        holder.gamesPlayedTextView.setText("🎮 Games: " + entry.getGamesPlayed());
        holder.enemiesDefeatedTextView.setText("💀 Defeated: " + entry.getEnemiesDefeated());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView,
                gamesPlayedTextView,
                enemiesDefeatedTextView,
                maxRoundTextView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView      = itemView.findViewById(R.id.usernameTextView);
            gamesPlayedTextView   = itemView.findViewById(R.id.tvGamesPlayed);
            enemiesDefeatedTextView = itemView.findViewById(R.id.tvEnemiesDefeated);
            maxRoundTextView      = itemView.findViewById(R.id.tvMaxRound);
        }
    }
}
