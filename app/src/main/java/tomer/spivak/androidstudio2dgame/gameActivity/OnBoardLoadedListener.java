package tomer.spivak.androidstudio2dgame.gameActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;

public interface OnBoardLoadedListener {
    void onBoardLoaded(DocumentSnapshot documentSnapshot, DifficultyLevel finalDifficultyLevel,
                       Long finalTimeSinceGameStart, int finalCurrentRound, int finalShnuzes, boolean finalDayTime);
}


