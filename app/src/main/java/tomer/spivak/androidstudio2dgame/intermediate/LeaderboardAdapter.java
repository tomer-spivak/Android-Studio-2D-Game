package tomer.spivak.androidstudio2dgame.intermediate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import tomer.spivak.androidstudio2dgame.R;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    private List<LeaderboardEntry> leaderboardList;

    public LeaderboardAdapter(List<LeaderboardEntry> leaderboardList) {
        this.leaderboardList = leaderboardList;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntry entry = leaderboardList.get(position);
        holder.uidTextView.setText(entry.getUid());
        holder.roundTextView.setText(String.valueOf(entry.getMaxRound()));
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView uidTextView, roundTextView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            uidTextView = itemView.findViewById(R.id.tvUsername);
            roundTextView = itemView.findViewById(R.id.tvRound);
        }
    }
}
