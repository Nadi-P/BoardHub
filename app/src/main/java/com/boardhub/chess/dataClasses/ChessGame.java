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

public class ChessGame implements Serializable {
    final private ChessPiece[][] board = new ChessPiece[8][8];

    private long whiteTime;
    private long blackTime;
    HashMap<String, Integer> whiteCaptures = new HashMap<>();
    HashMap<String, Integer> blackCaptures = new HashMap<>();

    King whiteKing;
    King blackKing;
    private boolean isWhiteTurn;

    //new game of chess
    public ChessGame(int mode, int customTime){
        this.isWhiteTurn = true;
        int time;
        if (mode == 0) time = ChessLogic.Constants.bulletModeDuration;
        else if (mode == 1) time = ChessLogic.Constants.blitzModeDuration;
        else if (mode == 2) time = ChessLogic.Constants.classicModeDuration;
        else time = customTime;

        whiteCaptures.put("King", 0);
        whiteCaptures.put("Queen", 0);
        whiteCaptures.put("Rook", 0);
        whiteCaptures.put("Bishop", 0);
        whiteCaptures.put("Knight", 0);
        whiteCaptures.put("Pawn", 0);

        blackCaptures.put("King", 0);
        blackCaptures.put("Queen", 0);
        blackCaptures.put("Rook", 0);
        blackCaptures.put("Bishop", 0);
        blackCaptures.put("Knight", 0);
        blackCaptures.put("Pawn", 0);

        //White pieces
        Rook whiteRook1 = new Rook(this, 0, 0, true);
        Knight whiteKnight1 = new Knight(this, 1, 0, true);
        Bishop whiteBishop1 = new Bishop(this, 2, 0, true);
        Queen whiteQueen = new Queen(this, 3, 0, true);
        King whiteKing = new King(this, 4, 0, true);
        Bishop whiteBishop2 = new Bishop(this, 5, 0, true);
        Knight whiteKnight2 = new Knight(this, 6, 0, true);
        Rook whiteRook2 = new Rook(this, 7, 0, true);

        for (int i = 0; i < 8; i++) {
            Pawn whitePawn = new Pawn(this, i, 1, true); // White pawns on row 6
            Pawn blackPawn = new Pawn(this, i, 6, false); // White pawns on row 6
        }

        //Black pieces
        Rook blackRook1 = new Rook(this, 0, 7, false);
        Knight blackKnight1 = new Knight(this, 1, 7, false);
        Bishop blackBishop1 = new Bishop(this, 2, 7, false);
        Queen blackQueen = new Queen(this, 3, 7, false);
        King blackKing = new King(this, 4, 7, false);
        Bishop blackBishop2 = new Bishop(this, 5, 7, false);
        Knight blackKnight2 = new Knight(this, 6, 7, false);
        Rook blackRook2 = new Rook(this, 7, 7, false);

        //link the rooks to the kings;
        whiteKing.SetRookLeft(whiteRook1);
        whiteKing.SetRookRight(whiteRook2);

        blackKing.SetRookLeft(blackRook1);
        blackKing.SetRookRight(blackRook2);

    }

    public ChessPiece[][] GetBoard() {
        return board;
    }
    public ChessPiece GetPieceAt(int x, int y) {
        return board[y][x];
    }
    public boolean IsWhiteTurn() {return isWhiteTurn; }
    public King GetWhiteKing() { return this.whiteKing; }
    public King GetBlackKing() { return this.blackKing; }
    public long GetWhiteTime() { return whiteTime; }
    public long GetBlackTime() { return blackTime; }
    public void SetIsWhiteTurn(boolean isWhiteTurn) { this.isWhiteTurn = isWhiteTurn; }
    public String GetCapturesString(boolean forWhite) {
        StringBuilder result = new StringBuilder();

        for (String type : ChessLogic.Constants.pieceTypes) {
            String key = ((forWhite) ? "b" : "w") + type;
            HashMap<String, Integer> captures = (forWhite) ? whiteCaptures : blackCaptures;
            Integer count = captures.get(type); // Get count from your map
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

    public void RecordCapture() {}
}
