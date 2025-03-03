package tomer.spivak.androidstudio2dgame.viewModel;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.model.EnemyManager;
import tomer.spivak.androidstudio2dgame.model.TurretManager;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Position;

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> gameState = new MutableLiveData<>();
    private String selectedBuildingType;
    private static final int NIGHT_THRESHOLD = 5000;
    EnemyManager enemyManager = new EnemyManager();
    TurretManager turretManager = new TurretManager();

    public void initBoardFromCloud(Cell[][] board) {
        gameState.setValue(new GameState(board, NIGHT_THRESHOLD));
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
            }
        } else {
            if (selectedCell.getObject() instanceof Building){
                selectedCell.removeObject();
            }
        }
    }

    public void placeBuilding(int row, int col, GameState current) {
        Cell cell = current.getGrid()[row][col];
        cell.placeBuilding((Building)ModelObjectFactory.create(selectedBuildingType,
                new Position(row, col)));
        gameState.postValue(current);
    }

    public void updateGameState(long deltaTime) {
        GameState current = gameState.getValue();

        if (current != null) {
            if (!current.getTimeOfDay()){
                checkDeath(current);
                if (enemyManager.getEnemies(current).isEmpty()){
                    //won
                    Win(current);
                    current.setTimeOfDay(true);
                    current.accumulateRound();
                    current.startTimerForNextRound();
                }
                if (isNotEmptyBuildings()){
                    List<Enemy> enemies = enemyManager.getEnemies(current);
                    turretManager.updateTurrets(current, enemies, deltaTime);
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

    private void Win(GameState current) {
        current.setGameStatus(GameStatus.WON);
    }

    private void Lose(GameState current) {
        current.setGameStatus(GameStatus.LOST);
    }

    private void checkDeath(GameState current ) {
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

        //place holder amount
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
        current.accumulateRound();
        current.startTimerForNextRound();
        updateGameState((long) (current.getTimeToNextRound() - 0.1));
        gameState.postValue(current);
    }

}