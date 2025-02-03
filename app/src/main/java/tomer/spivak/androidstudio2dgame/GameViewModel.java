package tomer.spivak.androidstudio2dgame;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import tomer.spivak.androidstudio2dgame.model.Building;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.model.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Position;

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> gameState = new MutableLiveData<>();
    private String selectedBuildingType;



    public void placeBuilding(int row, int col) {
        GameState current = gameState.getValue();
        if (current != null) {
            Cell cell = current.getGrid()[row][col];
            if (!cell.isOccupied() && !selectedBuildingType.isEmpty()) {
                cell.placeBuilding((Building)ModelObjectFactory.create(selectedBuildingType,
                        new Position(row, col)));
                gameState.setValue(current);
            }
        }
    }

    public LiveData<GameState> getGameState() {
        return gameState;
    }

    public void selectBuilding(String buildingType) {
        selectedBuildingType = buildingType;
    }

    public void initBoardFromCloud(Cell[][] board) {
        gameState.setValue(new GameState(board));
    }
}