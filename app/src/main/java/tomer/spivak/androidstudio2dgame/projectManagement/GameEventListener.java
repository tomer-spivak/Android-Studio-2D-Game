package tomer.spivak.androidstudio2dgame.projectManagement;
public interface GameEventListener {
    void onTick(long deltaTime);
    void onCellClicked(int row, int col);
}