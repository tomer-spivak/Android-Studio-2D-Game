package tomer.spivak.androidstudio2dgame.intermediate;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
    private final int maxRound;
    private final String displayName;
    private final int gamesPlayed;
    private final int enemiesDefeated;
    private final int victories;

    public LeaderboardEntry(int maxRound, String displayName, int gamesPlayed, int enemiesDefeated, int victories) {
        this.maxRound = maxRound;
        this.displayName = displayName;
        this.gamesPlayed = gamesPlayed;
        this.enemiesDefeated = enemiesDefeated;
        this.victories = victories;
    }

    public int getMaxRound() { return maxRound; }

    public int getGamesPlayed() { return gamesPlayed; }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getVictories() {
        return victories;
    }

    @Override
    public int compareTo(LeaderboardEntry other) {
        return Integer.compare(this.maxRound, other.maxRound);

    }
}

