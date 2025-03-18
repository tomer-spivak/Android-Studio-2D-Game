package tomer.spivak.androidstudio2dgame.viewModel;

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

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> gameState = new MutableLiveData<>();
    // New LiveData for sound events.
    private final MutableLiveData<String> soundEvent = new MutableLiveData<>();

    private String selectedBuildingType;
    private static final int NIGHT_THRESHOLD = 5000;
    EnemyManager enemyManager = new EnemyManager();
    TurretManager turretManager = new TurretManager();

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
        cell.placeBuilding((Building) ModelObjectFactory.create(selectedBuildingType,
                new Position(row, col), current.getDifficulty()));
    }

    public void updateGameState(long deltaTime) {
        GameState current = gameState.getValue();

        if (current != null) {
            current.addTime(deltaTime);
            if (!current.getTimeOfDay()){
                clearDeadObjects(current);
                if (enemyManager.getEnemies(current).isEmpty()){
                    //won
                    Win(current);
                    current.setTimeOfDay(true);
                    current.accumulateRound();
                    current.startTimerForNextRound();
                }
                if (isNotEmptyBuildings()){
                    // Example: Trigger turret attack sound if a turret fires.
                    if (turretManager.updateTurrets(current, enemyManager.getEnemies(current),
                            deltaTime))
                        triggerTurretAttackSound();  // <-- Call this when a turret attack happens

                    if(enemyManager.updateEnemies(current, deltaTime))
                        triggerEnemyAttackSound();
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
    public LiveData<String> getSoundEvent() {
        return soundEvent;
    }

    // Public methods to trigger sound events. Call these from within your game logic.
    public void triggerEnemyAttackSound() {
        soundEvent.postValue("enemyAttack");
    }

    public void triggerTurretAttackSound() {
        soundEvent.postValue("turretAttack");
    }

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
}
