package tomer.spivak.androidstudio2dgame.model;


import android.util.Log;

import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ExplodingBuilding;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ModelGameManager {
    private GameState state;
    private final EnemyManager enemyManager = new EnemyManager();
    private final TurretManager turretManager = new TurretManager();
    private SoundEffectManager soundEffects;
    private static final int NIGHT_THRESHOLD = 5000;
    private String selectedBuildingType;

    public ModelGameManager() {

    }

    public void init(Cell[][] board, DifficultyLevel difficulty) {
        state = new GameState(board, NIGHT_THRESHOLD, difficulty, 5 * (difficulty.ordinal() + 1));
        state.startTimerForNextRound();
        if(containsMainBuilding(board))
           return;
        createMainBase();
    }

    private boolean containsMainBuilding(Cell[][] board) {
        for (Cell[] cells : board) {
            for (Cell cell : cells) {
                if (cell.getObject() instanceof Building && cell.getObject().getType().equals("mainbuilding")){
                    completeMainBuilding((Building) cell.getObject());
                    return true;
                }
            }
        }
        return false;
    }

    private void completeMainBuilding(Building mainBuilding) {
        Cell upRightCell = state.getCellAt(new Position(mainBuilding.getPosition().getX(), mainBuilding.getPosition().getY() + 1));
        Cell downLeftCell = state.getCellAt(new Position(mainBuilding.getPosition().getX() + 1, mainBuilding.getPosition().getY()));
        Cell downRightCell = state.getCellAt(new Position(mainBuilding.getPosition().getX() + 1, mainBuilding.getPosition().getY() + 1));
        upRightCell.placeBuilding(mainBuilding);
        downLeftCell.placeBuilding(mainBuilding);
        downRightCell.placeBuilding(mainBuilding);

    }

    private void createMainBase() {
        Cell[][] fullBoard = state.getGrid();
        Cell[][] trimmed = trimOuterTwoRings(fullBoard);
        List<Cell> mainCells = getRandom2x2Block(trimmed);
        Position buildingPos = mainCells.get(0).getPosition();
        Building mainBuilding = (Building)ModelObjectFactory.create("mainbuilding", buildingPos, state.getDifficulty());
        for (Cell cell : mainCells) {
            cell.placeBuilding(mainBuilding);
        }
    }

    public Cell[][] trimOuterTwoRings(Cell[][] board) {
        int rows = board.length;
        int cols = board[0].length;

        int newRows = rows - 4;
        int newCols = cols - 4;

        Cell[][] trimmed = new Cell[newRows][newCols];

        for (int i = 2; i < rows - 2; i++) {
            if (cols - 2 - 2 >= 0) System.arraycopy(board[i], 2, trimmed[i - 2], 0, cols - 2 - 2);
        }

        return trimmed;
    }

    public List<Cell> getRandom2x2Block(Cell[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        Random rand = new Random();
        List<Cell> result = new ArrayList<>(4);

        // Make sure the board is big enough
        if (rows < 2 || cols < 2) return result;

        // Random top-left corner of 2x2 block
        int startRow = rand.nextInt(rows - 1);
        int startCol = rand.nextInt(cols - 1);

        result.add(board[startRow][startCol]);
        result.add(board[startRow][startCol + 1]);
        result.add(board[startRow + 1][startCol]);
        result.add(board[startRow + 1][startCol + 1]);

        return result;
    }

    public void handleCellClick(int row, int col) {
        Cell selectedCell = state.getGrid()[row][col];
        if (!selectedCell.isOccupied()){
            if (canPlaceBuilding(state, new Position(row, col))){
                placeSelectedBuilding(row, col, state);
            }
        } else if (!selectedCell.getObject().getType().equals("mainbuilding") && state.isDayTime() && getNumberOfBuildings() > 2) {
            Log.d("type", selectedCell.getObject().getType());
            removeBuilding(selectedCell, state);
        }
    }

    private boolean canPlaceBuilding(GameState state, Position position) {
        return selectedBuildingType != null && state.isDayTime() && state.getShnuzes() >=
                ModelObjectFactory.getPrice(selectedBuildingType) && !isOnFrame(state.getCellAt(position));
    }

    public boolean isOnFrame(Cell cell) {
        Cell[][] grid = state.getGrid();
        int numRows = grid.length;
        if (numRows == 0) return false;
        int numCols = grid[0].length;

        return cell.getPosition().getX() == 0 || cell.getPosition().getX() == numRows - 1 ||
                cell.getPosition().getY() == 0 || cell.getPosition().getY() == numCols - 1;
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

        Building building = (Building) selectedCell.getObject();
            building.stopSound();
            building.setSoundEffects(null);
            state.addShnuzes((int) ((building.getPrice() * building.getHealth())/building.getMaxHealth()));
            selectedCell.removeObject();


    }

    private void placeSelectedBuilding(int row, int col, GameState state) {
        Cell cell = state.getGrid()[row][col];
        Building building = (Building) ModelObjectFactory.create(selectedBuildingType, new Position(row, col), state.getDifficulty());
        building.setSoundEffects(soundEffects);
        cell.placeBuilding(building);
        building.setPosition(new Position(row, col));
        state.removeShnuzes(building.getPrice());
    }

    public void update(long deltaTime) {
        if (state == null) return;

        state.addTime(deltaTime);

        if (state.isDayTime()) {
            state.decreaseTimeToNextRound(deltaTime);
            if (state.getTimeToNextRound() <= 0) {
                state.setDayTime(false);
                startNight(state);
            }
            return;
        }

        // Night phase
        if (!containsMainBuilding(state.getGrid()) || getNumberOfBuildings() == 0) {
            initDefeat(state);
            return;
        }

        if (enemyManager.getEnemies(state).isEmpty()) {
            initRoundVictory(state);
            return;
        }

        turretManager.updateTurrets(state, enemyManager.getEnemies(state), deltaTime);
        enemyManager.updateEnemies(state, deltaTime);
        clearDeadObjects(state);
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
        if (object.getType().equals("explodingtower")){
            Log.d("cell", object.getType());
            executeExplosion(cell);
        }
    }

    private void executeExplosion(Cell explodingCell) {
        explodingCell.executeExplosion();
        Cell[][] board = getState().getGrid();
        int rows = board.length;
        int cols = board[0].length;
        int centerR = explodingCell.getPosition().getX();
        int centerC = explodingCell.getPosition().getY();
        int radius  = 1;  // change this to expand the ring
        for (int dr = -radius; dr <= radius; dr++) {
            for (int dc = -radius; dc <= radius; dc++) {
                int r = centerR + dr, c = centerC + dc;
                // skip out-of-bounds
                if (r < 0 || c < 0 || r >= rows || c >= cols) continue;
                // skip the center cell
                if (dr == 0 && dc == 0) continue;
                // only ring: at least one offset at full radius
                if (Math.max(Math.abs(dr), Math.abs(dc)) == radius) {
                    Cell cell = board[r][c];
                    ModelObject object = cell.getObject();
                    if (object instanceof Building)
                        continue;
                    if(object != null)
                        object.takeDamage(((ExplodingBuilding)explodingCell.getObject()).getDamage());
                    cell.executeExplosion();
                }
            }
        }
    }

    private void initDefeat(GameState state) {
        state.setGameStatus(GameStatus.LOST);
    }

    private void initRoundVictory(GameState state) {
        if (state.getCurrentRound() < state.getNumberOfRounds())
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
    private int getNumberOfBuildings() {
        Cell[][] grid = state.getGrid();
        List<Position> buildingPositions = new ArrayList<>();
        for (Cell[] cells : grid) {
            for (Cell cell : cells) {
                if (cell.isOccupied() && cell.getObject() instanceof Building
                        && cell.getObject().getPosition().equals(cell.getPosition())) {
                    buildingPositions.add(cell.getObject().getPosition());
                }
            }
        }
        return buildingPositions.size();
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

    public GameState getState() {
        return state;
    }

    public void setSelectedBuildingType(String type) {
        this.selectedBuildingType = type;
    }

    public boolean canStartGame() {
        return getNumberOfBuildings() > 1;
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