package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;

import java.util.ArrayList;

public class ChessPiece {
    protected String FENid;
    protected ChessGame game;
    protected boolean isWhite;
    protected int xPos;
    protected int yPos;
    protected int value;

    protected int imageResource;

    public ChessPiece(){}

    public ChessPiece(ChessGame game, int startX, int startY, boolean isWhite){
        this.isWhite = isWhite;
        this.xPos = startX;
        this.yPos = startY;

        this.game = game;
        this.game.GetBoard()[startY][startX] = this;

    }

    public boolean GetIsWhite() {
        return isWhite;
    }
    public int GetXPos() {
        return xPos;
    }
    public int GetYPos() {
        return yPos;
    }
    public int GetValue() {
        return value;
    }
    public int GetImageResource() {
        return imageResource;
    }
    public ChessGame GetGame() { return game; }
    public String GetFENid() { return FENid; }

    public void SetValue(int value) {
        this.value = value;
    }
    public void SetIsWhite(boolean isWhite) {
        this.isWhite = isWhite;
    }
    public void SetXPos(int xPos) {
        this.xPos = xPos;
    }
    public void SetYPos(int yPos) {
        this.yPos = yPos;
    }
    public void SetImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
    public void SetPosition(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void RemoveFromGame(){
        this.game.GetBoard()[this.yPos][this.xPos] = null;
    }

    public boolean isDifferentColor(ChessPiece piece){
        if (piece == null) return false; // A null square has no color
        return this.isWhite != piece.isWhite;
    }

    public void MoveTo(int xPos, int yPos, boolean isVirtual) {
        if (!ChessLogic.IsPositionInBoard(xPos, yPos)) return;
        this.game.GetBoard()[this.yPos][this.xPos] = null;
        this.xPos = xPos;
        this.yPos = yPos;
        this.game.GetBoard()[yPos][xPos] = this;
    }

    public void Capture(ChessMove move){
        ChessPiece[][] board = this.game.GetBoard();
        ChessPiece captured = move.capturedPiece;
        int x = move.targetX;
        int y = move.targetY;

        // 1. Handle En Passant
        if (move.isEnPassant && captured != null) {
            captured.RemoveFromGame(); // This clears the square next to the target
        }

        // 2. Handle Castling
        if (move.isRightCastling || move.isLeftCastling) {
            int rookX = move.isRightCastling ? 7 : 0;
            int rookTargetX = move.isRightCastling ? 5 : 3;
            ChessPiece rook = board[this.yPos][rookX];

            MoveTo(x, y, false); // Move King
            if (rook != null) rook.MoveTo(rookTargetX, y, false); // Move Rook
            return;
        }

        // 3. Standard Move/Capture
        if (captured != null) {
            this.player.AddCapture(captured);
            captured.RemoveFromGame();
        }
        MoveTo(x, y, false);

    }

    public ArrayList<ChessMove> GetMoves(){
        return null;
    };

    @Override
    public String toString() {
        String color = isWhite ? "White " : "Black ";
        String type = this.getClass().getSimpleName();
        String pos = "(" + xPos + ", " + yPos + ")";
        return color + " " +  type + " " + pos;
    }
}
