package tomer.spivak.androidstudio2dgame.gameObjects;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.Turret;

public class GameObjectManager {
    private final ArrayList<GameObject> gameObjectsViewsArrayList = new ArrayList<>();
    private final Context context;
    private final Cell[][] board;
    private final CellState[][] cellStates;
    private Point[][] centerCells;


    public GameObjectManager(Context context, int boardSize,
                             Point[][] centerCells) {
        this.context = context;
        this.centerCells = centerCells;

        board = new Cell[boardSize][boardSize];

        cellStates = new CellState[boardSize][boardSize];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = new Cell(new Position(i, j));
                cellStates[i][j] = CellState.NORMAL;
            }
        }



    }

    //takes everything in the model board into the game one
    public void updateGameBoardFromBoard(Cell[][] newBoard, float scale){
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                //if both of them dont have anything, it doesnt matter to us
                if (i >= newBoard.length || j >= newBoard[0].length)
                    continue;

                Log.d("broad", newBoard.length + ", " + newBoard[0].length);
                Log.d("broad", board.length + ", " + board[0].length);
                Log.d("broad", i + ", " + j);
                if ((!newBoard[i][j].isOccupied() && !board[i][j].isOccupied())){
                    CellState cellState = newBoard[i][j].getCellState();
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), cellState);
                    cellStates[i][j] = cellState;
                    continue;
                }

                if (newBoard[i][j].isOccupied() && !board[i][j].isOccupied()){
                    Log.d("board", newBoard[i][j].getObject().toString());
                    addObjectFromModelToView(newBoard[i][j].getObject(), i, j, scale);
                    CellState cellState = newBoard[i][j].getCellState();
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), newBoard[i][j].getObject(),
                            cellState);
                    cellStates[i][j] = cellState;
                    continue;
                }

                //if the old board has it but the new one doesnt,
                //we need to remove it.
                if (!newBoard[i][j].isOccupied() && board[i][j].isOccupied()){
                    removeGameObject(i, j);
                    CellState cellState = newBoard[i][j].getCellState();
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), cellState);
                    cellStates[i][j] = cellState;
                    continue;
                }

                if (newBoard[i][j].isOccupied() && board[i][j].isOccupied()){
                    CellState cellState = newBoard[i][j].getCellState();
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), newBoard[i][j].getObject()
                            , cellState);
                    cellStates[i][j] = cellState;
                    updateGameObject(newBoard[i][j], i, j, scale);
                }
            }
        }
    }

    private void updateGameObject(Cell cell, int i, int j, float scale) {
        removeGameObject(i, j);
        addObjectFromModelToView(cell.getObject(), i, j, scale);
    }

    private void removeGameObject(int i, int j) {
        for (int k = 0; k < gameObjectsViewsArrayList.size(); k++) {
            GameObject gameObject = gameObjectsViewsArrayList.get(k);
            if (gameObject.getPos().getX() == i && gameObject.getPos().getY() == j) {
                gameObjectsViewsArrayList.remove(k);
                break;
            }
        }
    }

    private void addObjectFromModelToView(ModelObject object, int centerX, int centerY, float scale) {
        GameObject gameObject = null;
        if (object instanceof Enemy){
            Enemy enemy = (Enemy) object;
            gameObject = GameObjectFactory.create(context, centerCells[centerX][centerY],
                    enemy.getType(), scale,
                    new Position(centerX, centerY),enemy.getEnemyState().
                            name().toLowerCase(),
                    enemy.getCurrentDirection().name().toLowerCase());
        } else if (object instanceof Building && !(object instanceof Turret)){
            Building building = (Building) object;
            gameObject = GameObjectFactory.create(context, centerCells[centerX][centerY], building.getType().toLowerCase(), scale,
                    new Position(centerX, centerY), building.getState().name().toLowerCase(), "");
        } else if (object instanceof Turret) {
            Turret turret = (Turret) object;
            gameObject = GameObjectFactory.create(context, centerCells[centerX][centerY], turret.getType().toLowerCase(), scale,
                    new Position(centerX, centerY), turret.getState().name().toLowerCase(), "");
        }
        addGameObject(gameObject);
    }

    //adds a building to the drawn buildings in order
    public void addGameObject(GameObject gameObject) {
        int i = 0;
        int size = gameObjectsViewsArrayList.size();
        while (i < size && gameObjectsViewsArrayList.get(i).getImagePoint().y <
                gameObject.getImagePoint().y) {
            i++;
        }
        gameObjectsViewsArrayList.add(i, gameObject); // Insert at the correct position
    }

    public void updateScaleForGameObjects(float scale) {
        for (GameObject gameObject : gameObjectsViewsArrayList) {
            gameObject.setScale(scale);
        }
    }

    public void updateGameObjectsPositions() {
        for (GameObject gameObject : gameObjectsViewsArrayList) {
            Position pos = gameObject.getPos();
            gameObject.setImagePoint(centerCells[pos.getX()][pos.getY()]);
        }
    }

    public ArrayList<GameObject> getGameObjectsViewsArrayList() {
        return gameObjectsViewsArrayList;
    }

    public CellState[][] getCellStates() {
        return cellStates;
    }

    public Cell[][] getBoard() {
        return board;
    }

    public void setCenterCells(Point[][] centerCells) {
        this.centerCells = centerCells;
    }
}
