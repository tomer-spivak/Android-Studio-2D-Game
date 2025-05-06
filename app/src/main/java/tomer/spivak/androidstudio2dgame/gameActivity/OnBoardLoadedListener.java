package tomer.spivak.androidstudio2dgame.gameActivity;


import java.util.Map;

import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;

public interface OnBoardLoadedListener {
    void onBoardLoaded(Map<String, Object> boardData, DifficultyLevel finalDifficultyLevel,
                       Long finalTimeSinceGameStart, int finalCurrentRound, int finalShnuzes, boolean finalDayTime);
}


