package tomer.spivak.androidstudio2dgame.viewModel;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tomer.spivak.androidstudio2dgame.model.Building;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.Enemy;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.model.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Monster;
import tomer.spivak.androidstudio2dgame.model.Pathfinder;
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
                gameState.postValue(current);
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


    // In GameViewModel
    public void updateGameState(long elapsedTime) {
        GameState current = gameState.getValue();
        if (current != null) {
            Log.d("debug", "elapsedTime: " + elapsedTime);
            if (elapsedTime > 5000 && current.getTimeOfDay()){
                startNight(current);
                gameState.postValue(current); // Use postValue for background thread
            }
        }
    }

    private void startNight(GameState current) {
        current.setTimeOfDay(false);

        //place holder amount
        int amount = 1;
        spawnEnemies(current, amount);
    }
    private void spawnEnemies(GameState current, int amount){
        //Log.d("enemyspawn", "cell to spawn enemies at: " + cellToSpawnEnemyAt
          //              .getPosition().toString());

        //spawns enemies
        Cell cellToSpawnEnemyAt = getRandomFramePointIndex(current.getGrid());
        Monster monster = (Monster) ModelObjectFactory.create("monster", new Position(0,0));
        cellToSpawnEnemyAt.spawnEnemy(monster);

        //get closet building
        Position closetBuilding = getClosetBuildingToEnemy(current, monster);





    }

    private Position getClosetBuildingToEnemy(GameState current, Enemy enemy) {
        Pathfinder pathfinder = new Pathfinder(current);

        List<Position> allBuildings = getBuildingPositions(current.getGrid());
        List<Position> buildingNeighbors = getNeighborPositions(allBuildings, current);

        Position closest = pathfinder.findClosestBuilding(enemy.getPosition(), buildingNeighbors);
        return closest;

    }

    private List<Position> getNeighborPositions(List<Position> positions, GameState current){
        List<Position> buildingPositions = new ArrayList<>();
        for (Position pos : positions) {
            List<Position> neighbors = pos.getNeighbors();
            for (Position neighbour : neighbors){
                if (current.isValidPosition(neighbour))
                    buildingPositions.add(neighbour);
            }
        }
        return buildingPositions;
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

    public Cell getRandomFramePointIndex(Cell[][] centerCells) {
        int rows = centerCells.length;
        int cols = centerCells[0].length;
        Random rand = new Random();

        // Total frame positions
        int leftColumnCount = rows - 2;
        int rightColumnCount = rows - 2;
        int totalFramePositions = cols + cols + leftColumnCount + rightColumnCount;

        // Generate random index from the frame positions
        int randChoice = rand.nextInt(totalFramePositions);

        if (randChoice < cols) {
            // First row
            return centerCells[0][randChoice];
        } else if (randChoice < cols + cols) {
            // Last row
            return centerCells[rows - 1][randChoice - cols];
        } else if (randChoice < cols + cols + leftColumnCount) {
            // First column (excluding first/last row)
            return centerCells[randChoice - cols - cols + 1][0];
        } else {
            // Last column (excluding first/last row)
            return centerCells[randChoice - cols - cols - leftColumnCount + 1][cols - 1];
        }
    }

}