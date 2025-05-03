package tomer.spivak.androidstudio2dgame.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.model.ModelGameManager;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> gameState = new MutableLiveData<>();
    private final ModelGameManager gameManager;

    public GameViewModel() {
        this.gameManager = new ModelGameManager();
    }

    public LiveData<GameState> getGameState() {
        return gameState;
    }

    public void initBoard(Cell[][] board, DifficultyLevel difficulty, int currentRound, int shnuzes) {
        gameManager.init(board, difficulty);
        gameState.setValue(gameManager.getState());
        gameManager.setCurrentRound(currentRound);
        if (shnuzes < 0)
            gameManager.initShnuzes();
        else
            gameManager.setShnuzes(shnuzes);
    }

    public void selectBuilding(String type) {
        gameManager.setSelectedBuildingType(type);
    }

    public void onCellClicked(int row, int col) {
        gameManager.handleCellClick(row, col);
        gameManager.setSelectedBuildingType(null);
        gameState.postValue(gameManager.getState());
    }

    public void tick(long deltaTime) {
        gameManager.update(deltaTime);
        gameState.postValue(gameManager.getState());
    }

    public void skipToNextRound() {
        gameManager.skipToNextRound();
        gameState.postValue(gameManager.getState());
    }

    public void setSoundEffects(SoundEffectManager effects) {
        gameManager.setSoundEffects(effects);
    }

    public boolean canStartGame() {
        return gameManager.canStartGame();
    }

    public int getRound() {
        return gameManager.getRound();
    }

    public int getEnemiesDefeated() {
        return gameManager.getEnemiesDefeated();
    }
}