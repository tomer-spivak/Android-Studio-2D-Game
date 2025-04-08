package tomer.spivak.androidstudio2dgame.viewModel;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.model.EnemyManager;
import tomer.spivak.androidstudio2dgame.model.TurretManager;
import tomer.spivak.androidstudio2dgame.modelAnimations.CellAnimationManager;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.music.SoundEffects;

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> gameState = new MutableLiveData<>();

    private String selectedBuildingType;
    private static final int NIGHT_THRESHOLD = 5000;
    EnemyManager enemyManager = new EnemyManager();
    TurretManager turretManager = new TurretManager();
    SoundEffects soundEffects;

    public void initBoardFromCloud(Cell[][] board, DifficultyLevel difficulty) {
        gameState.setValue(new GameState(board, NIGHT_THRESHOLD, difficulty));
        GameState current = gameState.getValue();
        if (current == null)
            return;
        current.startTimerForNextRound();
        gameState.postValue(current);
    }

    public void selectBuilding(String buildingType) {
        selectedBuildingType = buildingType;
    }

    public void onCellClicked(int row, int col) {
        GameState current = gameState.getValue();
        if (current == null)
            return;
        Cell selectedCell = current.getGrid()[row][col];
        if (!selectedCell.isOccupied()){
            if (canPlaceBuilding(current)){
                placeBuilding(row, col, current);
                selectedBuildingType = null;
                gameState.postValue(current);
            }
        } else {
            removeBuilding(selectedCell, current);
        }
    }

    private void removeBuilding(Cell selectedCell, GameState current) {
        int num = 0;
        Cell[][] grid = current.getGrid();
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

        if (selectedCell.getObject() instanceof Building && current.getTimeOfDay()){
            Building building = (Building) selectedCell.getObject();
            building.stopSound();
            building.setSoundEffects(null);

            current.addShnuzes(building.getPrice()/2);
            selectedCell.removeObject();
            gameState.postValue(current);
        }
    }

    private boolean canPlaceBuilding(GameState current) {
        return selectedBuildingType != null && current.getTimeOfDay() && current.getShnuzes() >= ModelObjectFactory.getPrice(selectedBuildingType);
    }

    public void placeBuilding(int row, int col, GameState current) {
        Cell cell = current.getGrid()[row][col];
        Building building = (Building) ModelObjectFactory.create(selectedBuildingType,
                new Position(row, col), current.getDifficulty());
        building.setSoundEffects(soundEffects);
        cell.placeBuilding(building);
        current.removeShnuzes(building.getPrice());
    }

    public void updateGameState(long deltaTime) {
        Log.d("update", String.valueOf(deltaTime));
        GameState current = gameState.getValue();
        if (current != null) {
            current.addTime(deltaTime);
            if (!current.getTimeOfDay()){
                if (enemyManager.getEnemies(current).isEmpty()){
                    //won
                    Win(current);
                    current.setTimeOfDay(true);
                    current.accumulateRound();
                    current.startTimerForNextRound();
                    current.addShnuzes(current.getCurrentRound() * 1000);
                }

                if (isNotEmptyBuildings()){
                    turretManager.updateTurrets(current, enemyManager.getEnemies(current), deltaTime);
                    enemyManager.updateEnemies(current, deltaTime);

                } else {
                    //raid ended with all buildings destroyed
                    Lose(current);
                }
                clearDeadObjects(current);
            }
            else {
                current.decreaseTimeToNextRound(deltaTime);
                if (current.getTimeToNextRound() <= 0){
                    current.setTimeOfDay(false);
                    startNight(current);
                }
            }

            gameState.postValue(current);
        }
    }

    private void cellDeath(GameState current, Cell cell) {
        ModelObject object = cell.getObject();
        if (object instanceof Enemy){
            Enemy enemy = (Enemy) object;
            CellAnimationManager.executeEnemyDeathAnimation(cell);
            current.addShnuzes(enemy.getReward());
        }
    }

    private void Win(GameState current) {
        current.setGameStatus(GameStatus.WON);
    }

    private void Lose(GameState current) {
        current.setGameStatus(GameStatus.LOST);
    }

    private void clearDeadObjects(GameState current ) {
        Cell[][] grid = current.getGrid();
        for (Cell[] cellRow : grid){
            for (Cell cell: cellRow){
                if (cell.getObject() != null){
                    ModelObject object = cell.getObject();
                    if (object.getHealth() <= 0){
                        cellDeath(current, cell);
                        cell.removeObject();
                    }
                }
            }
        }
    }

    private void startNight(GameState current) {
        current.setTimeOfDay(false);
        int amount = current.getRound();
        enemyManager.spawnEnemies(current, amount);
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

    public boolean isNotEmptyBuildings(){
        return !getBuildingPositions(Objects.requireNonNull(gameState.getValue()).getGrid())
                .isEmpty();
    }

    public LiveData<GameState> getGameState() {
        return gameState;
    }




    public void skipToNextRound() {
        GameState current = gameState.getValue();
        if (current == null)
            return;
        current.startTimerForNextRound();
        updateGameState((long) (current.getTimeToNextRound() - 0.1));
        gameState.postValue(current);
    }

    public int getRound() {
        return Objects.requireNonNull(gameState.getValue()).getRound();
    }

    public void setSoundEffects(SoundEffects soundEffects) {
        this.soundEffects = soundEffects;
        GameState gameState = this.gameState.getValue();
        if (gameState == null)
            return;
        Cell[][] grid = gameState.getGrid();
        for (Cell[] cells : grid) {
            for (Cell cell : cells) {
                if (cell.isOccupied()) {
                    cell.getObject().setSoundEffects(soundEffects);
                }
            }
        }
        enemyManager.setSoundEffects(soundEffects);
    }
}
