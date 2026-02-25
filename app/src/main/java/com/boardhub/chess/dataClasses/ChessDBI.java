package com.boardhub.chess.dataClasses;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import com.boardhub.chess.layouts.ChessActivity;
import com.boardhub.chess.layouts.ChessGameFragment;
import com.boardhub.chess.layouts.ChessStartGameMenu;
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


    public static User currentUser;

    private static final CollectionReference usersCollection =
            db.collection("users");
    private static final CollectionReference gamesCollection =
            db.collection("chessGames");
    private static final CollectionReference queueCollection =
            db.collection("match_queue");

    public static ListenerRegistration ListenToGame(String gameUID, EventListener<DocumentSnapshot> listener) {
        return gamesCollection.document(gameUID).addSnapshotListener(listener);
    }
    // -- login and sign up ---

    public static void AttemptLogin(String email, String password, Context context) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        // Fetch the user data from Firestore
                        FirebaseFirestore.getInstance().collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        // Map the document back to your User class
                                        User loggedInUser = documentSnapshot.toObject(User.class);
                                        ChessDBI.currentUser = loggedInUser;
                                        ChessUI.ReplaceChessScreen(ChessStartGameMenu.newInstance(false));
                                    }
                                });
                    } else {
                        Toast.makeText(context, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static void AttemptSignup(String email, String password, String username, Context context) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        User newUser = new User(uid, username, password);

                        // Save to Firestore under "users" collection
                        FirebaseFirestore.getInstance().collection("users")
                                .document(uid)
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    ChessDBI.currentUser = newUser;
                                    ChessUI.ReplaceChessScreen(ChessStartGameMenu.newInstance(false));
                                });
                    } else {
                        Toast.makeText(context, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
        void onMatchFound(String gameUID, boolean isWhite, String userUID, String opponentUID);
    }
    public static void AddPlayerToGameQueue(int
                                                    gameModeIndex, String preferredSide, OnMatchFoundListener listener) {
        String myUID = FirebaseAuth.getInstance().getUid();
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
                        JoinExistingRequest(match, preferredSide, myUID, listener);
                    } else {
                        CreateNewRequest(gameModeIndex, preferredSide, myUID, listener);
                    }
                }).addOnFailureListener(e -> {
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
                // Inside JoinExistingRequest transaction
                transaction.update(matchDoc.getReference(), "status", "MATCHED");
                transaction.update(matchDoc.getReference(), "gameUID", gameUID);
                transaction.update(matchDoc.getReference(), "creatorIsWhite", creatorIsWhite);
                transaction.update(matchDoc.getReference(), "opponentUID", myUID); // Add this line!

                return new Object[]{gameUID, !creatorIsWhite, myUID, matchDoc.getId()}; // Joiner's result
            }
            return null;
        }).addOnSuccessListener(result -> {
            if (result != null) {
                Object[] data = (Object[]) result;
                listener.onMatchFound((String) data[0], (boolean) data[1], (String) data[2], (String) data[3]);
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

        queueCollection.document(myUID).set(request);

        queueCollection.document(myUID).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && "MATCHED".equals(snapshot.getString("status"))) {
                String gameUID = snapshot.getString("gameUID");
                boolean isWhite = snapshot.getBoolean("creatorIsWhite");
                String opponentUID = snapshot.getString("opponentUID"); // Get the UID here

                listener.onMatchFound(gameUID, isWhite, myUID, opponentUID); // Pass it to listener
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

    public static void GetUserWithUID(String uid, UserCallback callback) {
        usersCollection.document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                callback.onCallback(user); // Send the user back via interface
            }
        });
    }

    // Define this interface in your class
    public interface UserCallback {
        void onCallback(User user);
    }
}
