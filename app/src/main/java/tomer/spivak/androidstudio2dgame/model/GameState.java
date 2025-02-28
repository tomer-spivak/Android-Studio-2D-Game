package tomer.spivak.androidstudio2dgame.model;


import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;

public class GameState {
    private final Cell[][] grid; // 2D grid of cells
    private final int nightThreshold;
    private boolean timeOfDay; // true for day, false for night
    private long timeToNextRound;
    //true is morning, false is night
    private GameStatus gameStatus;
    private int currentRound;

    public GameState(Cell[][] grid, int nightThreshold) {
        this.grid = grid;
        this.timeOfDay = true;
        this.currentRound = 1;
        this.nightThreshold = 5000;
    }

    public boolean isValidPosition(Position pos) {
        // Check if the position is within grid bounds
        return pos.getX() >= 0 && pos.getX() < grid.length &&
                pos.getY() >= 0 && pos.getY() < grid[0].length;
    }

    public Cell getCellAt(Position pos) {
        return grid[pos.getX()][pos.getY()];
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public boolean getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(boolean b) {
        this.timeOfDay = b;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public long getTimeToNextRound() {
        return timeToNextRound;
    }

    public void setTimeToNextRound(long timeOfNextRound) {
        this.timeToNextRound = timeOfNextRound;
    }

    public void decreaseTimeToNextRound(long delta){
        this.timeToNextRound -= delta;
    }


    public void accumulateRound() {
        currentRound++;
    }

    public void startTimerForNextRound() {
        this.timeToNextRound = (long) nightThreshold * currentRound;
    }

    public int getRound() {
        return currentRound;
    }
}