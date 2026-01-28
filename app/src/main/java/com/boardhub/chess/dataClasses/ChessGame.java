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
    private ChessPlayer whitePlayer;
    private ChessPlayer blackPlayer;
    private boolean isWhiteTurn;

    //new game of chess
    public ChessGame(int mode, int customTime){
        this.isWhiteTurn = true;
        int time;
        if (mode == 0) time = ChessLogic.Constants.bulletModeDuration;
        else if (mode == 1) time = ChessLogic.Constants.blitzModeDuration;
        else if (mode == 2) time = ChessLogic.Constants.classicModeDuration;
        else time = customTime;

        HashMap<String, Integer> whiteCaptures = new HashMap<>();
        HashMap<String, Integer> blackCaptures = new HashMap<>();

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

        this.whitePlayer = new ChessPlayer(this, true, time, null, whiteCaptures);
        this.blackPlayer = new ChessPlayer(this, false, time, null, blackCaptures);

        //White pieces
        Rook whiteRook1 = new Rook(this.whitePlayer, 0, 0);
        Knight whiteKnight1 = new Knight(this.whitePlayer, 1, 0);
        Bishop whiteBishop1 = new Bishop(this.whitePlayer, 2, 0);
        Queen whiteQueen = new Queen(this.whitePlayer, 3, 0);
        King whiteKing = new King(this.whitePlayer, 4, 0);
        Bishop whiteBishop2 = new Bishop(this.whitePlayer, 5, 0);
        Knight whiteKnight2 = new Knight(this.whitePlayer, 6, 0);
        Rook whiteRook2 = new Rook(this.whitePlayer, 7, 0);

        for (int i = 0; i < 8; i++) {
            Pawn whitePawn = new Pawn(this.whitePlayer, i, 1); // White pawns on row 6
            Pawn blackPawn = new Pawn(this.blackPlayer, i, 6); // White pawns on row 6
        }

        //Black pieces
        Rook blackRook1 = new Rook(this.blackPlayer, 0, 7);
        Knight blackKnight1 = new Knight(this.blackPlayer, 1, 7);
        Bishop blackBishop1 = new Bishop(this.blackPlayer, 2, 7);
        Queen blackQueen = new Queen(this.blackPlayer, 3, 7);
        King blackKing = new King(this.blackPlayer, 4, 7);
        Bishop blackBishop2 = new Bishop(this.blackPlayer, 5, 7);
        Knight blackKnight2 = new Knight(this.blackPlayer, 6, 7);
        Rook blackRook2 = new Rook(this.blackPlayer, 7, 7);

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
    public ChessPlayer GetWhitePlayer() {
        return whitePlayer;
    }
    public ChessPlayer GetBlackPlayer() {
        return blackPlayer;
    }
    public boolean GetIsWhiteTurn() {return isWhiteTurn; }

    public void SetWhitePlayer(ChessPlayer whitePlayer) {
        this.whitePlayer = whitePlayer;
    }
    public void SetBlackPlayer(ChessPlayer blackPlayer) {
        this.blackPlayer = blackPlayer;
    }
    public void SetIsWhiteTurn(boolean isWhiteTurn) { this.isWhiteTurn = isWhiteTurn; }

    public String getBoardAsString() {
        StringBuilder sb = new StringBuilder();

        // Chess coordinates usually show row 8 at the top, which is index 0 in your array
        sb.append("   a b c d e f g h\n"); // Column labels
        sb.append("  -----------------\n");

        for (int y = 0; y < 8; y++) {
            sb.append(8 - y).append("| "); // Row labels (8 to 1)

            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board[y][x];

                if (piece == null) {
                    // Unicode for a small middle dot
                    sb.append("*  ");
                } else {
                    // Get the character from constants based on piece type and color
                    sb.append(getPieceChar(piece)).append(" ");
                }
            }
            sb.append("|").append(8 - y).append("\n");
        }

        sb.append("  -----------------\n");
        sb.append("   a b c d e f g h\n");

        return sb.toString();
    }
    private String getPieceChar(ChessPiece piece) {
        boolean isWhite = piece.GetIsWhite();

        if (piece instanceof Pawn)   return isWhite ? ChessLogic.Constants.whitePawnText.toString() : ChessLogic.Constants.blackPawnText.toString();
        if (piece instanceof Rook)   return isWhite ? ChessLogic.Constants.whiteRookText.toString() : ChessLogic.Constants.blackRookText.toString();
        if (piece instanceof Knight) return isWhite ? ChessLogic.Constants.whiteKnightText.toString() : ChessLogic.Constants.blackKnightText.toString();
        if (piece instanceof Bishop) return isWhite ? ChessLogic.Constants.whiteBishopText.toString() : ChessLogic.Constants.blackBishopText.toString();
        if (piece instanceof Queen)  return isWhite ? ChessLogic.Constants.whiteQueenText.toString() : ChessLogic.Constants.blackQueenText.toString();
        if (piece instanceof King)   return isWhite ? ChessLogic.Constants.whiteKingText.toString() : ChessLogic.Constants.blackKingText.toString();

        return "?";
    }
}
