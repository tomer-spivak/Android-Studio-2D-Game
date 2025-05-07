package tomer.spivak.androidstudio2dgame.gameActivity;

public interface GameEventListener {
    void onTick(long deltaTime);
    void onCellClicked(int row, int col);
}