package com.boardhub.chess.dataClasses;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.boardhub.R;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    public static ListenerRegistration ListenToGame(String gameUID, EventListener<DocumentSnapshot> listener) {
        return gamesCollection.document(gameUID).addSnapshotListener(listener);
    }
    // -- login and sign up ---

    public static void AttemptLogin(String email, String password, Context context) {
        Log.i("ChessDBI", "AttemptLogin called for email=" + email);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    Log.i("ChessDBI", "Login auth complete, success=" + task.isSuccessful());
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        // Fetch the user data from Firestore
                        FirebaseFirestore.getInstance().collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        User loggedInUser = documentSnapshot.toObject(User.class);
                                        if (loggedInUser != null) loggedInUser.setUid(uid);
                                        ChessDBI.currentUser = loggedInUser;
                                        ChessUI.ReplaceChessScreen(ChessStartGameMenu.newInstance(false));
                                    } else {
                                        Log.w("ChessDBI", "Login: user doc does not exist for uid=" + uid);
                                        Toast.makeText(context, "Login Failed: user record not found", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ChessDBI", "Login: firestore fetch failed", e);
                                    Toast.makeText(context, "Login Failed (Firestore): " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Exception ex = task.getException();
                        Log.e("ChessDBI", "Login: auth failed", ex);
                        Toast.makeText(context, "Login Failed: " + (ex != null ? ex.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                    }
                });
    }
    public static void AttemptSignup(String email, String password, String username, Context context) {
        Log.i("ChessDBI", "AttemptSignup called for email=" + email);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    Log.i("ChessDBI", "Signup auth complete, success=" + task.isSuccessful());
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        Log.i("ChessDBI", "Signup auth ok uid=" + uid + ", writing firestore doc...");

                        User newUser = new User(uid, username, password);

                        // Save to Firestore under "users" collection
                        FirebaseFirestore.getInstance().collection("users")
                                .document(uid)
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.i("ChessDBI", "Signup firestore write ok, replacing screen");
                                    ChessDBI.currentUser = newUser;
                                    ChessUI.ReplaceChessScreen(ChessStartGameMenu.newInstance(false));
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ChessDBI", "Signup: firestore write failed", e);
                                    Toast.makeText(context, "Signup Failed (Firestore): " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Exception ex = task.getException();
                        Log.e("ChessDBI", "Signup: auth failed", ex);
                        Toast.makeText(context, "Signup Failed: " + (ex != null ? ex.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
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

    public static void DeleteGame(String gameUID) {
        if (gameUID == null) return;
        gamesCollection.document(gameUID).delete()
                .addOnSuccessListener(v -> Log.i("ChessDBI", "DeleteGame: deleted " + gameUID))
                .addOnFailureListener(e -> Log.e("ChessDBI", "DeleteGame: failed for " + gameUID, e));
    }

    // --- Handle Queueing ---

    public interface OnMatchFoundListener {
        void onMatchFound(String gameUID, boolean isWhite, String userUID, String opponentUID);
    }
    public static void AddPlayerToGameQueue(int gameModeIndex, String preferredSide, OnMatchFoundListener listener) {
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
    public static void RecordGameResult(String resultField) {
        // resultField is one of "chessWins", "chessLosses", "chessDraws".
        Log.i("ChessDBI", "RecordGameResult called field=" + resultField + " currentUser=" + (currentUser != null ? currentUser.getUid() : "null"));
        if (currentUser == null) {
            Log.w("ChessDBI", "RecordGameResult: currentUser is null, skipping");
            return;
        }
        String uid = currentUser.getUid();
        if (uid == null) {
            Log.w("ChessDBI", "RecordGameResult: uid is null, skipping");
            return;
        }

        if ("chessWins".equals(resultField)) currentUser.AddChessWin();
        else if ("chessLosses".equals(resultField)) currentUser.AddChessLoss();
        else if ("chessDraws".equals(resultField)) currentUser.AddChessDraw();

        usersCollection.document(uid).update(resultField, FieldValue.increment(1))
                .addOnSuccessListener(v -> Log.i("ChessDBI", "RecordGameResult: firestore updated " + resultField + " for " + uid))
                .addOnFailureListener(e -> Log.e("ChessDBI", "RecordGameResult: firestore update FAILED for " + uid, e));
    }

    public static void RemovePlayerFromGameQueue() {
        String myUID = FirebaseAuth.getInstance().getUid();
        if (myUID != null) {
            queueCollection.document(myUID).delete();
        }
    }

    // Define this interface in your class
    public static void getUserFromUID(String uid, UserCallback callback) {
        usersCollection.document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 1. Extract Firestore data
                String username = documentSnapshot.getString("username");
                String password = documentSnapshot.getString("password");
                int wins = documentSnapshot.getLong("chessWins") != null ?
                        documentSnapshot.getLong("chessWins").intValue() : 0;
                int losses = documentSnapshot.getLong("chessLosses") != null ?
                        documentSnapshot.getLong("chessLosses").intValue() : 0;
                int draws = documentSnapshot.getLong("chessDraws") != null ?
                        documentSnapshot.getLong("chessDraws").intValue() : 0;

                // 2. Now fetch the Image Uri from Storage
                getImageWithUID(uid, new ImageCallback() {
                    @Override
                    public void onSuccess(Uri uri) {
                        System.out.println("Success");
                        // Success: Image found, pass URI to constructor
                        User user = new User(uid, uri.toString(), username, password, wins, losses, draws);
                        callback.onCallback(user);
                    }

                    @Override
                    public void onFailure(String error) {
                        System.out.println("Error" + error);
                        // Failure: No image found, pass null for URI (falls back to pawn icon)
                        User user = new User(uid, null, username, password, wins, losses, draws);
                        callback.onCallback(user);
                    }
                });

            } else {
                callback.onCallback(null);
            }
        }).addOnFailureListener(e -> callback.onCallback(null));
    }

    // Define the interface for the callback
    public interface UserCallback {
        void onCallback(User user);
    }
    public static void UpdateUser(User user) {

        usersCollection.document(user.getUid()).set(user);
    }
    public static void getImageWithUID(String uid, ImageCallback callback) {
        StorageReference storageRef = storage.getReference()
                .child("profile_pics/" + uid + ".jpg");

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            callback.onSuccess(uri);
        }).addOnFailureListener(e -> {
            callback.onFailure(e.getMessage());
        });
    }
    public interface ImageCallback {
        void onSuccess(Uri uri);
        void onFailure(String error);
    }
    public static void LoadImageToView(Activity activity, ImageView imageView, String urlString){
        if (urlString == null || urlString.isEmpty()) {
            imageView.setImageResource(R.drawable.chess_piece_white_pawn);
            return;
        }

        new Thread(() -> {
            try {
                // 1. Convert String to URL object
                java.net.URL url = new java.net.URL(urlString);

                // 2. Open connection and decode the stream into a Bitmap
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());

                // 3. Switch back to UI thread (with safety check)
                if (activity != null) {
                    activity.runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle error by showing the pawn icon
                if (activity != null) {
                    activity.runOnUiThread(() -> imageView.setImageResource(R.drawable.chess_piece_white_pawn));
                }
            }
        }).start();
    }
}
