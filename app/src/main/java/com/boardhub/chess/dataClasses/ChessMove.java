package com.boardhub.chess.dataClasses;

import com.boardhub.chess.pieces.ChessPiece;

public class ChessMove {
    public ChessPiece movedPiece;
    public ChessPiece capturedPiece;
    public int targetX;
    public int targetY;
    public boolean isRightCastling;
    public boolean isLeftCastling;
    public boolean isEnPassant;

    public ChessMove(ChessPiece movedPiece, ChessPiece capturedPiece, int targetX, int targetY) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.targetX = targetX;
        this.targetY = targetY;
    }
}
