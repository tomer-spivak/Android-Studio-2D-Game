package tomer.spivak.androidstudio2dgame;


import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Cell;

public class FirebaseRepository {
    private final FirebaseFirestore db;
    private final FirebaseUser user;

    public FirebaseRepository(Context context) {
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void saveBoard(Cell[][] board, OnSuccessListener<Void> onSuccess,
                          OnFailureListener onFailure) {
        if (user == null) return;

        Map<String, Object> boardData = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            List<Map<String, Object>> rowData = new ArrayList<>();
            for (int j = 0; j < board[i].length; j++) {
                rowData.add(board[i][j].toMap()); // Convert each Cell to a map
            }
            boardData.put("row_" + i, rowData);
        }

        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("board objects")
                .set(boardData)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
        for (Map.Entry<String, Object> entry : boardData.entrySet()) {
            Log.d("debug", "board data: " + entry.getKey() + ": " + entry.getValue());
        }
    }


    public void loadBoard(OnSuccessListener<DocumentSnapshot> onSuccess,
                          OnFailureListener onFailure) {
        if (user == null) {
            Log.d("debug", "user is null");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("board objects")
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
