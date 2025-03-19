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
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.music.SoundEffects;

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> gameState = new MutableLiveData<>();
    // New LiveData for sound events.

    private String selectedBuildingType;
    private static final int NIGHT_THRESHOLD = 5000;
    EnemyManager enemyManager = new EnemyManager();
    TurretManager turretManager = new TurretManager();
    boolean deadObjectsExist = false;
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
            if (selectedBuildingType != null && current.getTimeOfDay()){
                placeBuilding(row, col, current);
                selectedBuildingType = null;
                gameState.postValue(current);
            }
        } else {
            if (selectedCell.getObject() instanceof Building && current.getTimeOfDay() &&
                    canRemoveBuilding(current)){
                selectedCell.removeObject();
                gameState.postValue(current);
            }
        }
    }

    private boolean canRemoveBuilding(GameState current) {
        int num = 0;
        Cell[][] grid = current.getGrid();
        for (Cell[] cells : grid) {
            for (Cell value : cells) {
                if (value.isOccupied() && value.getObject() instanceof Building) {
                    num++;
                }
                if (num == 2)
                    return true;
            }
        }
        return false;
    }

    public void placeBuilding(int row, int col, GameState current) {
        Cell cell = current.getGrid()[row][col];
        Building building = (Building) ModelObjectFactory.create(selectedBuildingType,
                new Position(row, col), current.getDifficulty());
        building.setSoundEffects(soundEffects);
        cell.placeBuilding(building);
    }

    public void updateGameState(long deltaTime) {
        Log.d("update", String.valueOf(deltaTime));
        GameState current = gameState.getValue();
        if (current != null) {
            current.addTime(deltaTime);
            if (!current.getTimeOfDay()){
                //clearDeadObjects(current);

                if (deadObjectsExist) {
                    clearDeadObjects(current);
                    deadObjectsExist = false;
                }
                if (checkDeadObjects(current)){
                    deadObjectsExist = true;
                }

                if (enemyManager.getEnemies(current).isEmpty()){
                    //won
                    Win(current);
                    current.setTimeOfDay(true);
                    current.accumulateRound();
                    current.startTimerForNextRound();
                }

                if (isNotEmptyBuildings()){
                    // Example: Trigger turret attack sound if a turret fires.
                    turretManager.updateTurrets(current, enemyManager.getEnemies(current),
                            deltaTime);
                    enemyManager.updateEnemies(current, deltaTime);

                } else {
                    //raid ended with all buildings destroyed
                    Lose(current);
                }
            }
            else {
                current.decreaseTimeToNextRound(deltaTime);
                if (current.getTimeToNextRound() <= 0){
                    current.setTimeOfDay(false);
                    startNight(current);
                }
            }
            gameState.postValue(current); // Use postValue for background thread
        }
    }

    private boolean checkDeadObjects(GameState gameState) {
        Cell[][] grid = gameState.getGrid();
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                ModelObject object = cell.getObject();
                if (object == null)
                    continue;
                boolean dead = object.getHealth() <= 0;
               if (dead)
                   return true;
            }
        }
        return false;
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
                        cell.removeObject();
                    }
                }
            }
        }
    }

    private void startNight(GameState current) {
        current.setTimeOfDay(false);
        // Place holder amount
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

    // Expose the sound event LiveData



    public void skipToNextRound() {
        GameState current = gameState.getValue();
        if (current == null)
            return;
        //current.accumulateRound();
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
