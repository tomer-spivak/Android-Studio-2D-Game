package tomer.spivak.androidstudio2dgame.intermediate;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
    private final int maxRound;
    private final String displayName;
    private final int gamesPlayed;
    private final int enemiesDefeated;

    public LeaderboardEntry(int maxRound, String displayName, int gamesPlayed, int enemiesDefeated) {
        this.maxRound = maxRound;
        this.displayName = displayName;
        this.gamesPlayed = gamesPlayed;
        this.enemiesDefeated = enemiesDefeated;
    }



        // Constructor, getters, and setters...public String getDisplayName() { return displayName; }
    public int getMaxRound() { return maxRound; }
        public int getGamesPlayed() { return gamesPlayed; }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int compareTo(LeaderboardEntry other) {
        return Integer.compare(this.maxRound, other.maxRound);

    }
}

