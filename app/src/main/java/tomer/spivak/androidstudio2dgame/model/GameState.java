package tomer.spivak.androidstudio2dgame.model;


import android.util.Log;

import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;

public class GameState {
    private final Cell[][] grid;
    private final int nightThreshold;
    private boolean timeOfDay;
    private long timeToNextRound;
    private GameStatus gameStatus;
    private int currentRound;
    private final DifficultyLevel difficulty;
    private long currentTimeOfGame;
    private int shnuzes;

    public GameState(Cell[][] grid, int nightThreshold, DifficultyLevel difficulty) {
        this.grid = grid;
        this.timeOfDay = true;
        this.currentRound = 1;
        this.nightThreshold = nightThreshold;
        this.difficulty = difficulty;
        initShnuzes();
        currentTimeOfGame = 0;
    }

    private void initShnuzes() {
        int difficulty = 4 - (this.difficulty.ordinal() + 1);
        this.shnuzes = difficulty * 5000;
    }

    public boolean isValidPosition(Position pos) {
        return pos.getX() >= 0 && pos.getX() < grid.length &&
                pos.getY() >= 0 && pos.getY() < grid[0].length;
    }

    public Cell getCellAt(Position pos) {
        return grid[pos.getX()][pos.getY()];
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public int getShnuzes() {
        return shnuzes;
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

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void addTime(long deltaTime) {
        currentTimeOfGame += deltaTime;
    }

    public long getCurrentTimeOfGame() {
        Log.d("time", String.valueOf(currentTimeOfGame));
        return currentTimeOfGame;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void addShnuzes(int shnuzes) {
        this.shnuzes += shnuzes;
    }

    public void removeShnuzes(int shnuzes) {
        this.shnuzes -= shnuzes;
    }

}