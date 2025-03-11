package tomer.spivak.androidstudio2dgame.gameManager;

public interface GameViewListener {
    void onCellClicked(int row, int col);
    void onBuildingSelected(String buildingType);
    void updateGameState(long elapsedTime);
}