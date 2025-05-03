package tomer.spivak.androidstudio2dgame.model;


import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

import java.util.ArrayList;
import java.util.List;


public class ModelGameManager {
    private GameState state;
    private final EnemyManager enemyManager = new EnemyManager();
    private final TurretManager turretManager = new TurretManager();
    private SoundEffectManager soundEffects;
    private static final int NIGHT_THRESHOLD = 5000;
    private String selectedBuildingType;
    private int lastRound;

    public ModelGameManager() {

    }

    public void init(Cell[][] board, DifficultyLevel difficulty) {
        state = new GameState(board, NIGHT_THRESHOLD, difficulty);
        state.startTimerForNextRound();
        lastRound = (board.length - 1) * 4;
    }

    public GameState getState() {
        return state;
    }

    public void handleCellClick(int row, int col) {
        Cell selectedCell = state.getGrid()[row][col];
        if (!selectedCell.isOccupied()){
            if (canPlaceBuilding(state)){
                placeBuilding(row, col, state);
                selectedBuildingType = null;
            }
        } else {
            removeBuilding(selectedCell, state);
        }
    }

    private boolean canPlaceBuilding(GameState state) {
        return selectedBuildingType != null && state.isDayTime() && state.getShnuzes() >=
                ModelObjectFactory.getPrice(selectedBuildingType);
    }

    private void removeBuilding(Cell selectedCell, GameState state) {
        int num = 0;
        Cell[][] grid = state.getGrid();
        for (Cell[] cells : grid) {
            for (Cell value : cells) {
                if (value.isOccupied() && value.getObject() instanceof Building) {
                    num++;
                }
                if (num == 2)
                    break;
            }
        }
        if (num < 2)
            return;

        if (selectedCell.getObject() instanceof Building && state.isDayTime()){
            Building building = (Building) selectedCell.getObject();
            building.stopSound();
            building.setSoundEffects(null);
            state.addShnuzes((int) ((building.getPrice() * building.getHealth())/building.getMaxHealth()));
            selectedCell.removeObject();
        }

    }

    private void placeBuilding(int row, int col, GameState state) {
        Cell cell = state.getGrid()[row][col];
        Building building = (Building) ModelObjectFactory.create(selectedBuildingType,
                new Position(row, col), state.getDifficulty());
        building.setSoundEffects(soundEffects);
        cell.placeBuilding(building);
        state.removeShnuzes(building.getPrice());
    }

    public void update(long deltaTime) {
        if (state != null) {
            state.addTime(deltaTime);
            if (!state.isDayTime()){
                if (enemyManager.getEnemies(state).isEmpty()){
                    //won
                    initRoundVictory(state);
                }

                if (buildingsExist()){
                    turretManager.updateTurrets(state, enemyManager.getEnemies(state), deltaTime);
                    enemyManager.updateEnemies(state, deltaTime);

                } else {
                    //raid ended with all buildings destroyed
                    initDefeat(state);
                }
                clearDeadObjects(state);
            }
            else {
                state.decreaseTimeToNextRound(deltaTime);
                if (state.getTimeToNextRound() <= 0){
                    state.setDayTime(false);
                    startNight(state);
                }
            }
        }
    }

    private void startNight(GameState state) {
        state.setDayTime(false);
        int amount = state.getRound();
        if (!enemiesExist())
            enemyManager.spawnEnemies(state, amount);

    }



    private void clearDeadObjects(GameState state) {
        Cell[][] grid = state.getGrid();
        for (Cell[] cellRow : grid){
            for (Cell cell: cellRow){
                if (cell.getObject() != null){
                    ModelObject object = cell.getObject();
                    if (object.getHealth() <= 0){
                        executeCellDeath(state, cell);
                        cell.removeObject();
                    }
                }
            }
        }

    }

    private void executeCellDeath(GameState state, Cell cell) {
        ModelObject object = cell.getObject();
        if (object instanceof Enemy){
            Enemy enemy = (Enemy) object;
            cell.executeEnemyDeathAnimation();
            state.addShnuzes(enemy.getReward());
            state.incrimentEnemiesDefeated();
        }

    }

    private void initDefeat(GameState state) {
        state.setGameStatus(GameStatus.LOST);
    }

    private void initRoundVictory(GameState state) {
        if (state.getCurrentRound() <= lastRound)
            continueToNextRound();
        else {
            state.setGameStatus(GameStatus.WON);
        }
    }

    private void continueToNextRound() {
        state.setDayTime(true);
        state.accumulateRound();
        state.startTimerForNextRound();
        state.addShnuzes(state.getCurrentRound() * 1000);
    }

    private boolean buildingsExist() {
        return !getBuildingPositions(state.getGrid()).isEmpty();
    }
    private boolean enemiesExist() {
        Cell[][] board = state.getGrid();
        for (Cell[] row: board){
            for (Cell cell: row){
                if (cell.getObject() instanceof Enemy)
                    return true;
            }
        }
        return false;
    }
    private List<Position> getBuildingPositions(Cell[][] grid) {
        List<Position> buildingPositions = new ArrayList<>();
        for (Cell[] cells : grid) {
            for (Cell cell : cells) {
                if (cell.isOccupied() && cell.getObject() instanceof Building) {
                    buildingPositions.add(cell.getObject().getPosition());
                }
            }
        }
        return buildingPositions;
    }

    public void skipToNextRound() {
        state.startTimerForNextRound();
        state.decreaseTimeToNextRound(state.getTimeToNextRound() - 1);
    }

    public void setSoundEffects(SoundEffectManager effects) {
        this.soundEffects = effects;
        if (state == null)
            return;
        Cell[][] grid = state.getGrid();
        for (Cell[] cells : grid) {
            for (Cell cell : cells) {
                if (cell.isOccupied()) {
                    cell.getObject().setSoundEffects(soundEffects);
                }
            }
        }
        enemyManager.setSoundEffects(soundEffects);

    }

    public void setSelectedBuildingType(String type) {
        this.selectedBuildingType = type;
    }

    public boolean canStartGame() {
        return buildingsExist();
    }

    public int getRound() {
        return state.getCurrentRound();
    }

    public void setCurrentRound(int currentRound) {
        state.setCurrentRound(currentRound);
    }

    public void setShnuzes(int shnuzes) {
        state.setShnuzes(shnuzes);
    }

    public void initShnuzes() {
        state.initShnuzes();
    }

    public void setDayTime(boolean dayTime) {
        state.setDayTime(dayTime);
    }
}