package tomer.spivak.androidstudio2dgame.logic;


import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;

public class GameState {
    private final Cell[][] grid;
    private boolean dayTime;
    private long timeToNextRound;
    private GameStatus gameStatus;
    private int currentRound;
    private final DifficultyLevel difficulty;
    private long currentTimeOfGame;
    private int shnuzes;
    private int enemiesDefeated = 0;
    private final int numberOfRounds;

    public GameState(Cell[][] grid, DifficultyLevel difficulty, int numberOfRounds) {
        this.grid = grid;
        this.numberOfRounds = numberOfRounds;
        this.dayTime = true;
        this.currentRound = 1;
        this.difficulty = difficulty;
        currentTimeOfGame = 0;
        gameStatus = GameStatus.PLAYING;
        this.shnuzes = (4 - (this.difficulty.ordinal() + 1)) * 5000;
        startTimerForNextRound();
    }

    public boolean isValidPosition(Position pos) {
        return pos.getX() >= 0 && pos.getX() < grid.length && pos.getY() >= 0 && pos.getY() < grid[0].length;
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

    public void setDayTime(boolean dayTime) {
        this.dayTime = dayTime;
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

    public void startTimerForNextRound() {
        currentTimeOfGame = 0;
        this.timeToNextRound = (long) 5000 * currentRound;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void addTime(long deltaTime) {
        currentTimeOfGame += deltaTime;
    }

    public long getCurrentTimeOfGame() {
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
    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public void setShnuzes(int shnuzes) {
        this.shnuzes = shnuzes;
    }

    public boolean getDayTime() {
        return dayTime;
    }

    public void incrementEnemiesDefeated(){
        enemiesDefeated++;
    }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }
}