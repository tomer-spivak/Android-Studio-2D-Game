package tomer.spivak.androidstudio2dgame.model;


public class GameState {
    private final Cell[][] grid; // 2D grid of cells

    public GameState(Cell[][] grid) {
        this.grid = grid;
    }

    // Getters and setters
    public Cell[][] getGrid() {
        return grid;
    }

}