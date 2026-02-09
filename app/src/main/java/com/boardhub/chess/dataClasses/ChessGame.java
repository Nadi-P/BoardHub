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
import java.util.UUID;

public class ChessGame implements Serializable {
    private String UID;
    boolean assignedIsWhite;

    final private ChessPiece[][] board = new ChessPiece[8][8];
    private boolean isWhiteTurn;

    private long whiteTime;
    HashMap<String, Integer> whiteCaptures = new HashMap<>();
    King whiteKing;

    private long blackTime;
    HashMap<String, Integer> blackCaptures = new HashMap<>();
    King blackKing;

    //new game of chess
    public ChessGame(String gameUID, boolean assignedIsWhite, int modeIndex){
        this.UID = gameUID;
        this.assignedIsWhite = assignedIsWhite;

        this.isWhiteTurn = true;

        whiteTime = ChessLogic.Constants.modesDurations[modeIndex];
        blackTime = ChessLogic.Constants.modesDurations[modeIndex];

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

        final int whiteStartRow = 0;
        final int blackStartRow = 7;

        //White pieces
        Rook whiteRook1 = new Rook(this, 0, whiteStartRow, true);
        Knight whiteKnight1 = new Knight(this, 1, whiteStartRow, true);
        Bishop whiteBishop1 = new Bishop(this, 2, whiteStartRow, true);
        Queen whiteQueen = new Queen(this, 3, whiteStartRow, true);
        King whiteKing = new King(this, 4, whiteStartRow, true);
        Bishop whiteBishop2 = new Bishop(this, 5, whiteStartRow, true);
        Knight whiteKnight2 = new Knight(this, 6, whiteStartRow, true);
        Rook whiteRook2 = new Rook(this, 7, whiteStartRow, true);

        for (int i = 0; i < 8; i++) {
            Pawn whitePawn = new Pawn(this, i, whiteStartRow + 1, true); // White pawns on row 6
            Pawn blackPawn = new Pawn(this, i, blackStartRow - 1, false); // White pawns on row 6
        }

        //Black pieces
        Rook blackRook1 = new Rook(this, 0, blackStartRow, false);
        Knight blackKnight1 = new Knight(this, 1, blackStartRow, false);
        Bishop blackBishop1 = new Bishop(this, 2, blackStartRow, false);
        Queen blackQueen = new Queen(this, 3, blackStartRow, false);
        King blackKing = new King(this, 4, blackStartRow, false);
        Bishop blackBishop2 = new Bishop(this, 5, blackStartRow, false);
        Knight blackKnight2 = new Knight(this, 6, blackStartRow, false);
        Rook blackRook2 = new Rook(this, 7, blackStartRow, false);

        //link the rooks to the kings;
        whiteKing.SetRookLeft(whiteRook1);
        whiteKing.SetRookRight(whiteRook2);

        blackKing.SetRookLeft(blackRook1);
        blackKing.SetRookRight(blackRook2);

        this.whiteKing = whiteKing;
        this.blackKing = blackKing;

        ChessDBI.SaveGame(this);
    }

    public String GetUID() {
        return UID;
    }
    public ChessPiece[][] GetBoard() {
        return board;
    }
    public ChessPiece GetPieceAt(int x, int y) {
        return board[y][x];
    }
    public boolean IsWhiteTurn() {return isWhiteTurn; }
    public King GetKing(boolean forWhite) { return (forWhite) ? whiteKing : blackKing; }
    public long GetTime(boolean forWhite) { return (forWhite) ? whiteTime : blackTime; }
    public boolean GetAssignedIsWhite() { return assignedIsWhite; }
    public void SetIsWhiteTurn(boolean isWhiteTurn) { this.isWhiteTurn = isWhiteTurn; }

    public void SetTime(long time, boolean forWhite) {
        if (forWhite) whiteTime = time;
        else blackTime = time;
    }

    public String GetCapturesString(boolean forWhite) {
        StringBuilder result = new StringBuilder();

        for (String type : ChessLogic.Constants.pieceTypes) {
            String key = ((forWhite) ? "b" : "w") + type;
            HashMap<String, Integer> captures = (forWhite) ? whiteCaptures : blackCaptures;
            Integer count = captures.get(type); // Get count from your map
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

    public void RecordCapture(ChessPiece captured) {
        if (captured == null) return;
        String type = captured.getClass().getSimpleName();
        boolean isWhite = !captured.GetIsWhite();
        HashMap<String, Integer> captures = (isWhite) ? whiteCaptures : blackCaptures;
        captures.put(type, captures.get(type) + 1);
    }

    public String GetBoardFEN(){
        StringBuilder boardBuilder = new StringBuilder();
        ChessPiece[][] board = this.board;
        for (int y = 7; y >= 0; y--){
            for (int x = 0; x < 8; x++){
                ChessPiece piece = board[y][x];
                if (piece != null) boardBuilder.append(piece.GetFENid());
                else boardBuilder.append(ChessLogic.Constants.FENChars[12]);
            }
        }
        return boardBuilder.toString();
    }
}
