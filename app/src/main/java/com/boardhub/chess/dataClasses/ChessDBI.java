package com.boardhub.chess.dataClasses;

import android.widget.EditText;

import com.boardhub.chess.layouts.ChessGameFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ChessDBI {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final CollectionReference gamesCollection =
            db.collection("chessGames");
    private static final CollectionReference queueCollection =
            db.collection("match_queue");

    public static ListenerRegistration ListenToGame(String gameUID, EventListener<DocumentSnapshot> listener) {
        return gamesCollection.document(gameUID).addSnapshotListener(listener);
    }
    // -- login and sign up ---

    public interface AuthCallback {
        void onResult(boolean success, String message);
    }

    public static void AttemptLogin(EditText etEmail, EditText etPassword, AuthCallback callback) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            callback.onResult(false, "Please fill all fields");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(true, "Login Successful");
                    } else {
                        callback.onResult(false, "Login Failed: " + task.getException().getMessage());
                    }
                });
    }

    public static void AttemptSignUp(EditText etEmail, EditText etPassword, AuthCallback callback) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic Validation
        if (email.isEmpty() || password.length() < 6) {
            callback.onResult(false, "Email empty or password too short (min 6 chars)");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(true, "Registration Successful");
                    } else {
                        callback.onResult(false, "Registration Failed: " + task.getException().getMessage());
                    }
                });
    }

    // --- Handle Game Updates ---

    public static void SaveGame(ChessGame game) {
        Map<String, Object> packet = new HashMap<>();
        packet.put("gameUID", game.GetUID());
        packet.put("boardFEN", game.GetBoardFEN());
        packet.put("isWhiteTurn", game.IsWhiteTurn());
        packet.put("whiteTime", game.GetTime(true));
        packet.put("blackTime", game.GetTime(false));

        // Default values for first-time setup
        packet.put("xInitial", -1);
        packet.put("yInitial", -1);
        packet.put("xTarget", -1);
        packet.put("yTarget", -1);
        packet.put("isRightCastling", false);
        packet.put("isLeftCastling", false);
        packet.put("isEnPassant", false);
        packet.put("isPromotion", false);
        packet.put("promotionPieceIndex", -1);

        gamesCollection.document(game.GetUID()).set(packet);
    }

    public static void SaveMove(ChessMove move) {
        if (move.game == null) return;
        move.whiteTime = move.game.GetTime(true);
        move.blackTime = move.game.GetTime(false);

        Map<String, Object> packet = move.FormatToPacket();
        gamesCollection.document(move.game.GetUID()).set(packet);
    }

    // --- Handle Queueing ---

    public interface OnMatchFoundListener {
        void onMatchFound(String gameUID, boolean isWhite);
    }

    public static void AddPlayerToGameQueue(int
            gameModeIndex, String preferredSide, OnMatchFoundListener listener) {
        String myUID = FirebaseAuth.getInstance().getUid();

        System.out.println("Connected UID: " + myUID);
        System.out.println(FirebaseAuth.getInstance().getCurrentUser());

        queueCollection
                .whereEqualTo("gameModeIndex", gameModeIndex)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot match = null;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        if (doc.getId().equals(myUID)) continue;
                        if (isCompatible(preferredSide, doc.getString("side"))) {
                            match = doc;
                            break;
                        }
                    }

                    if (match != null) {
                        System.out.println("Attempting to join");
                        JoinExistingRequest(match, preferredSide, myUID, listener);
                    } else {
                        System.out.println("Creating new Queue");
                        CreateNewRequest(gameModeIndex, preferredSide, myUID, listener);
                    }
                }).addOnFailureListener(e -> {
                    System.out.println("Error: " + e.getMessage());
                    System.out.println("Attempt Failed Miserably");
                });
    }

    private static void JoinExistingRequest(
            DocumentSnapshot matchDoc, String mySide, String myUID, OnMatchFoundListener listener) {
        db.runTransaction(transaction -> {
            DocumentSnapshot freshSnap = transaction.get(matchDoc.getReference());
            if (freshSnap.exists() && "WAITING".equals(freshSnap.getString("status"))) {

                // Resolve Colors
                String creatorSide = freshSnap.getString("side");
                boolean creatorIsWhite;
                if (creatorSide.equals("WHITE")) creatorIsWhite = true;
                else if (creatorSide.equals("BLACK")) creatorIsWhite = false;
                else creatorIsWhite = Math.random() < 0.5; // Coin flip for Random vs Random

                String gameUID = UUID.randomUUID().toString();

                // Create the Game
                Map<String, Object> gameData = new HashMap<>();
                gameData.put("gameUID", gameUID);
                gameData.put("whitePlayer", creatorIsWhite ? matchDoc.getId() : myUID);
                gameData.put("blackPlayer", creatorIsWhite ? myUID : matchDoc.getId());
                gameData.put("isWhiteTurn", true);
                gameData.put("whiteTime", 600000); // Default or from mode
                gameData.put("blackTime", 600000);

                transaction.set(gamesCollection.document(gameUID), gameData);

                // Notify Creator via their queue doc
                transaction.update(matchDoc.getReference(), "status", "MATCHED");
                transaction.update(matchDoc.getReference(), "gameUID", gameUID);
                transaction.update(matchDoc.getReference(), "creatorIsWhite", creatorIsWhite);

                return new Object[]{gameUID, !creatorIsWhite}; // Joiner's result
            }
            return null;
        }).addOnSuccessListener(result -> {
            if (result != null) {
                Object[] data = (Object[]) result;
                System.out.println("Joined Game");
                listener.onMatchFound((String) data[0], (boolean) data[1]);
            }
        });
    }

    private static void CreateNewRequest(
            int modeIndex, String side, String myUID, OnMatchFoundListener listener) {
        Map<String, Object> request = new HashMap<>();
        request.put("gameModeIndex", modeIndex);
        request.put("side", side);
        request.put("status", "WAITING");
        request.put("timestamp", FieldValue.serverTimestamp());

        System.out.println("Created New Game");
        queueCollection.document(myUID).set(request);

        queueCollection.document(myUID).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && "MATCHED".equals(snapshot.getString("status"))) {
                String gameUID = snapshot.getString("gameUID");
                boolean isWhite = snapshot.getBoolean("creatorIsWhite");
                listener.onMatchFound(gameUID, isWhite);
                snapshot.getReference().delete();
            }
        });
    }

    private static boolean isCompatible(
            String mySide, String opponentSide) {
        if (mySide.equals("RANDOM") || opponentSide.equals("RANDOM")) return true;
        if (mySide.equals("WHITE") && opponentSide.equals("BLACK")) return true;
        if (mySide.equals("BLACK") && opponentSide.equals("WHITE")) return true;
        return false;
    }

    public static void RemovePlayerFromGameQueue() {
        String myUID = FirebaseAuth.getInstance().getUid();
        if (myUID != null) {
            queueCollection.document(myUID).delete();
        }
    }
}
