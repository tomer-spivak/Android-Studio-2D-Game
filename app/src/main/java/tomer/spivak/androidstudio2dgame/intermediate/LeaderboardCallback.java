package tomer.spivak.androidstudio2dgame.intermediate;

import java.util.List;

public interface LeaderboardCallback {
    void onLeaderboardFetched(List<LeaderboardEntry> leaderboardEntries);
}