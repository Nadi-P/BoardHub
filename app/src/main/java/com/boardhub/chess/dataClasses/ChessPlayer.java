package com.boardhub.chess.dataClasses;

import com.boardhub.chess.pieces.Bishop;
import com.boardhub.chess.pieces.ChessPiece;
import com.boardhub.chess.pieces.King;
import com.boardhub.chess.pieces.Knight;
import com.boardhub.chess.pieces.Pawn;
import com.boardhub.chess.pieces.Queen;
import com.boardhub.chess.pieces.Rook;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class ChessPlayer implements Serializable {
    private ChessGame game;
    private HashSet<ChessPiece> pieces = new HashSet<>();
    private int timeLeft; // mil-secs
    private boolean isWhite;
    private King king;
    private HashMap<String, Integer> captures;


    public ChessPlayer(ChessGame game, boolean isWhite, int time, HashSet<ChessPiece> pieces, HashMap<String, Integer> captures){
        this.game = game;
        this.isWhite = isWhite;
        this.timeLeft = time;
        this.captures = captures;
        if (pieces != null) this.pieces = pieces;
    }

    // Getter and Setter for pieces
    public HashSet<ChessPiece> GetPieces() {
        return pieces;
    }
    public boolean GetIsWhite() {
        return isWhite;
    }
    public int GetTimeLeft() {
        return timeLeft;
    }
    public ChessGame GetGame() {
        return game;
    }
    public King GetKing(){
        return king;
    }

    public void SetPieces(HashSet<ChessPiece> pieces) {
        this.pieces = pieces;
    }
    public void SetTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }
    public void SetGame(ChessGame game) {
        this.game = game;
    }
    public void SetIsWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }
    public void SetKing(King king){
        this.king = king;
    }

    public void AddCapture(ChessPiece piece) {
        if (piece == null) {
            System.out.println("Null piece passed to AddCapture");
            return;
        }
        String pieceName = piece.getClass().getSimpleName();
        System.out.println("Adding capture for " + pieceName);
        this.captures.put(pieceName, this.captures.get(pieceName) + 1);
    }

    public String GetCapturesString() {
        StringBuilder result = new StringBuilder();

        for (String type : ChessLogic.Constants.pieceTypes) {
            String key = ((isWhite) ? "b" : "w") + type;
            Integer count = this.captures.get(type); // Get count from your map
            System.out.println("Count for " + key + ": " + count);
            if (count != null && count > 0) {
                char icon = ChessLogic.Constants.textIconsMap.get(key);
                for (int i=0; i < count; i++){
                    result.append(icon);
                }
                result.append(" ");
            }
        }

        return result.toString().trim();
    }

}
