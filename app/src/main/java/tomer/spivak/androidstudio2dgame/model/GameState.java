package tomer.spivak.androidstudio2dgame.model;


import android.util.Log;

import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;

public class GameState {
    private final Cell[][] grid; // 2D grid of cells
    private boolean timeOfDay; // true for day, false for night
    private long accumulatedDayTime;
    private long timeOfNextRound;
    private GameStatus gameStatus;

    public GameState(Cell[][] grid) {
        this.grid = grid;
        this.timeOfDay = true;
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

    public long getTimeUntilNextRound(){
        Log.d("debug", String.valueOf(timeOfNextRound));
        Log.d("debug", String.valueOf(accumulatedDayTime));
        return timeOfNextRound - accumulatedDayTime;
    }

    public void setTimeOfNextRound(long timeOfNextRound) {
        this.timeOfNextRound = timeOfNextRound;
    }

    public void setAccumulatedDayTime(long accumulatedDayTime) {
        this.accumulatedDayTime = accumulatedDayTime;
    }
}