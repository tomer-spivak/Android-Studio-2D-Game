package tomer.spivak.androidstudio2dgame.intermediate;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
    private final int maxRound;
    private final String displayName;

    public LeaderboardEntry(int maxRound, String displayName) {
        this.maxRound = maxRound;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxRound() {
        return maxRound;
    }

    @Override
    public int compareTo(LeaderboardEntry other) {
        return Integer.compare(this.maxRound, other.maxRound); // Descending order

    }
}

