package tomer.spivak.androidstudio2dgame.logic;

import tomer.spivak.androidstudio2dgame.logic.modelEnums.BuildingState;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.projectManagement.SoundEffectManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModelGameManager {
    private GameState state;
    private final EnemyManager enemyManager = new EnemyManager();
    private SoundEffectManager soundEffects;
    private String selectedBuildingType;
    private boolean sunrise = false;

    public void init(Cell[][] board, DifficultyLevel difficulty) {
        state = new GameState(board, difficulty, 5 * (difficulty.ordinal() + 1));
        if(containsMainBuilding(board))
           return;

        int newBoardSize = state.getGrid().length - 4;

        Cell[][] smallerBoard = new Cell[newBoardSize][newBoardSize];

        for (int i = 2; i < state.getGrid().length - 2; i++) {
            for (int j = 2; j < state.getGrid().length - 2; j++) {
                smallerBoard[i - 2][j - 2] = state.getGrid()[i][j];
            }
        }

        Random rand = new Random();
        List<Cell> mainCells = new ArrayList<>(4);
        int startRow = rand.nextInt(smallerBoard.length - 1);
        int startCol = rand.nextInt(smallerBoard.length - 1);
        mainCells.add(smallerBoard[startRow][startCol]);
        mainCells.add(smallerBoard[startRow][startCol + 1]);
        mainCells.add(smallerBoard[startRow + 1][startCol]);
        mainCells.add(smallerBoard[startRow + 1][startCol + 1]);
        Building mainBuilding = (Building)ModelObjectFactory.create("mainbuilding", mainCells.get(0).getPosition(), state.getDifficulty());
        for (Cell cell : mainCells) {
            cell.placeBuilding(mainBuilding);
        }
    }

    private boolean containsMainBuilding(Cell[][] board) {
        for (Cell[] cells : board) {
            for (Cell cell : cells) {
                if (cell.getObject() instanceof Building && cell.getObject().getType().equals("mainbuilding")){
                    Building mainBuilding = (Building) cell.getObject();
                    Cell upRightCell = state.getCellAt(new Position(mainBuilding.getPosition().getX(), mainBuilding.getPosition().getY() + 1));
                    Cell downLeftCell = state.getCellAt(new Position(mainBuilding.getPosition().getX() + 1, mainBuilding.getPosition().getY()));
                    Cell downRightCell = state.getCellAt(new Position(mainBuilding.getPosition().getX() + 1, mainBuilding.getPosition().getY() + 1));
                    upRightCell.placeBuilding(mainBuilding);
                    downLeftCell.placeBuilding(mainBuilding);
                    downRightCell.placeBuilding(mainBuilding);
                    return true;
                }
            }
        }
        return false;
    }

    public void handleCellClick(int row, int col) {
        Cell selectedCell = state.getGrid()[row][col];
        if (!selectedCell.isOccupied()){
            Cell cellClicked = state.getGrid()[row][col];
            if (selectedBuildingType != null && state.getDayTime() && state.getShnuzes() >= ModelObjectFactory.getPrice(selectedBuildingType) &&
                    !(cellClicked.getPosition().getX() == 0 || cellClicked.getPosition().getX() == state.getGrid().length - 1 || cellClicked.getPosition().getY() == 0 ||
                    cellClicked.getPosition().getY() == state.getGrid().length - 1)){
                Position position = new Position(row, col);
                Building building = (Building) ModelObjectFactory.create(selectedBuildingType, position, state.getDifficulty());
                building.setSoundEffects(soundEffects);
                cellClicked.placeBuilding(building);
                building.setPosition(position);
                state.removeShnuzes(ModelObjectFactory.getPrice(building.getType()));
            }
        } else if (!selectedCell.getObject().getType().equals("mainbuilding") && state.getDayTime() && getNumberOfBuildings() > 2) {
            Building building = (Building) selectedCell.getObject();
            building.stopSound();
            building.setSoundEffects(null);
            state.addShnuzes((int) ((ModelObjectFactory.getPrice(building.getType()) * building.getHealth())/building.getMaxHealth()));
            selectedCell.removeObject();
        }
    }

    public void update(long deltaTime) {
        if (state == null)
            return;
        deltaTime = Math.min(100, deltaTime);
        state.addTime(deltaTime);
        if (state.getDayTime()) {
            if (sunrise) {
                for (Cell[] cells : state.getGrid()) {
                    for (Cell cell : cells) {
                        cell.resetAnimation();
                        if(cell.getObject() instanceof Building)
                            ((Building) cell.getObject()).setState(BuildingState.IDLE);
                    }
                }
                sunrise = false;
            }
            state.decreaseTimeToNextRound(deltaTime);
            if (state.getTimeToNextRound() <= 0) {
                state.setDayTime(false);
                int amount = state.getCurrentRound();
                if (enemyManager.getEnemies(state).isEmpty())
                    enemyManager.spawnEnemies(state, amount);
            }
        }
        else {
            // Night phase
            if (!containsMainBuilding(state.getGrid())) {
                state.setGameStatus(GameStatus.LOST);
                return;
            }

            if (enemyManager.getEnemies(state).isEmpty()) {
                if (state.getCurrentRound() < state.getNumberOfRounds()){
                    state.setDayTime(true);
                    sunrise = true;
                    state.setCurrentRound(state.getCurrentRound() + 1);
                    state.startTimerForNextRound();
                    state.addShnuzes(state.getCurrentRound() * 1000);
                }
                else {
                    state.setGameStatus(GameStatus.WON);
                }
                return;
            }

            List<Enemy> enemies = enemyManager.getEnemies(state);
            for (Cell[] cells : state.getGrid()) {
                for (Cell cell : cells) {
                    cell.updateAnimation(deltaTime);
                    if(cell.getObject() instanceof Building){
                        ((Building) cell.getObject()).update(deltaTime);
                        if(cell.getObject() instanceof Turret){
                            Turret turret = (Turret) cell.getObject();
                            turret.update(state, deltaTime);
                            if (turret.executeAttack(enemies)){
                                ArrayList<Position> positionsToAttack = turret.getCellsToAttack();
                                for (Position pos : positionsToAttack){
                                    state.getCellAt(pos).executeBurntAnimation();
                                }
                            }
                        }
                    }
                }
            }

            enemyManager.updateEnemies(state, deltaTime);
            for (Cell[] cells : state.getGrid()){
                for (Cell cell: cells){
                    if (!cell.isOccupied()){
                        continue;
                    }
                    ModelObject modelObject = cell.getObject();
                    if (modelObject.getHealth() <= 0){
                        if (modelObject instanceof Enemy){
                            Enemy enemy = (Enemy) modelObject;
                            cell.executeEnemyDeathAnimation();
                            state.addShnuzes(enemy.getReward());
                            state.incrementEnemiesDefeated();
                        }
                        if (modelObject.getType().equals("explodingtower")){
                            cell.executeExplosion();
                            int centerX = cell.getPosition().getX();
                            int centerY = cell.getPosition().getY();
                            for (int i = -1; i <= 1; i++) {
                                for (int j = -1; j <= 1; j++) {
                                    int x = centerX + i, y = centerY + j;
                                    if (x >= 0 && y >= 0 && x < state.getGrid().length && y < state.getGrid().length){
                                        Cell cellToExplode = state.getGrid()[x][y];
                                        ModelObject modelObjectToExplode = cellToExplode.getObject();
                                        if (modelObjectToExplode instanceof Enemy){
                                            modelObjectToExplode.takeDamage(((ExplodingBuilding) cell.getObject()).getDamage());
                                        }
                                        if(!(modelObjectToExplode instanceof Building)){
                                            cellToExplode.executeExplosion();
                                        }
                                    }
                                }
                            }
                        }
                        cell.removeObject();
                    }
                }
            }
        }
    }

    private int getNumberOfBuildings() {
        int num = 0;
        for (Cell[] cells : state.getGrid()) {
            for (Cell cell : cells) {
                if (cell.getObject() instanceof Building && cell.getObject().getPosition().equals(cell.getPosition())) {
                    num++;
                }
            }
        }
        return num;
    }

    public void skipToNextRound() {
        state.resetTimer();
    }

    public void setSoundEffects(SoundEffectManager effects) {
        this.soundEffects = effects;
        if (state == null)
            return;
        for (Cell[] cells : state.getGrid()) {
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

    public void setDayTime(boolean dayTime) {
        state.setDayTime(dayTime);
    }
}