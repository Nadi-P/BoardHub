package com.boardhub.chess.dataClasses;

import com.boardhub.chess.pieces.ChessPiece;
import com.boardhub.chess.pieces.King;

import java.io.Serializable;
import java.util.HashSet;

public class ChessPlayer implements Serializable {
    private HashSet<ChessPiece> pieces = new HashSet<>();
    private int timeLeft; // mil-secs
    private boolean isWhite;
    private ChessGame board;
    private King king;

    public ChessPlayer(ChessGame board, boolean isWhite, int time, HashSet<ChessPiece> pieces){
        this.board = board;
        this.isWhite = isWhite;
        this.timeLeft = time;
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
    public ChessGame GetBoard() {
        return board;
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
    public void SetBoard(ChessGame board) {
        this.board = board;
    }
    public void SetIsWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }
    public void SetKing(King king){
        this.king = king;
    }

}
