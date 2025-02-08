package tomer.spivak.androidstudio2dgame.model;



public class GameState {
    private final Cell[][] grid; // 2D grid of cells
    private boolean timeOfDay; // true for day, false for night


    public GameState(Cell[][] grid) {
        this.grid = grid;
        this.timeOfDay = true;
    }

    // Getters and setters
    public Cell[][] getGrid() {
        return grid;
    }

    public boolean getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(boolean b) {
        this.timeOfDay = b;
    }
    public boolean isValidPosition(Position pos) {
        // Check if the position is within grid bounds
        return pos.getX() >= 0 && pos.getX() < grid.length &&
                pos.getY() >= 0 && pos.getY() < grid[0].length;
    }
    public Cell getCellAt(Position pos) {
        return grid[pos.getX()][pos.getY()];
    }


}