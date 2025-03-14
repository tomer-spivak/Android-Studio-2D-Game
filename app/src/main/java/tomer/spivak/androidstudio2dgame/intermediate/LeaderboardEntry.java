package tomer.spivak.androidstudio2dgame.intermediate;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
    private final String username;
    private final int maxRound;

    public LeaderboardEntry(String uid, int maxRound) {
        this.username = uid;
        this.maxRound = maxRound;
    }

    public String getUid() {
        return username;
    }

    public int getMaxRound() {
        return maxRound;
    }

    @Override
    public int compareTo(LeaderboardEntry other) {
        return Integer.compare(this.maxRound, other.maxRound); // Descending order

    }
}

