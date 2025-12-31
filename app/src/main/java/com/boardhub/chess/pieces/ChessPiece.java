package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.HashMap;

public class ChessPiece {
    protected ChessPlayer player;
    protected ChessGame board;
    protected boolean isWhite;
    protected int xPos;
    protected int yPos;
    protected int value;

    protected int imageResource;

    public ChessPiece(){}

    public ChessPiece(ChessPlayer player, int startX, int startY){
        this.player = player;
        this.isWhite = player.GetIsWhite();
        this.xPos = startX;
        this.yPos = startY;

        this.player.GetPieces().add(this);
        this.board = this.player.GetBoard();
        this.MoveTo(this.xPos, this.yPos);
    }

    public ChessPlayer GetPlayer() {
        return player;
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
    public ChessGame GetBoard() { return board; }

    public void SetPlayer(ChessPlayer player) {
        this.player = player;
    }
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
        this.player.GetPieces().remove(this);
        this.board.GetBoard()[this.yPos][this.xPos] = null;
    }

    public void AddToGame(){
        this.player.GetPieces().add(this);
        this.board.GetBoard()[this.yPos][this.xPos] = this;
    }

    public void MoveTo(int xPos, int yPos) {
        // Only call the internal logic, do not call PseudoMoveTo here
        executeInternalMove(xPos, yPos);
    }

    public void PseudoMoveTo(int xPos, int yPos) {
        executeInternalMove(xPos, yPos);
    }

    // Create a private helper to actually move the data
    private void executeInternalMove(int xPos, int yPos) {
        this.board.GetBoard()[this.yPos][this.xPos] = null;
        this.xPos = xPos;
        this.yPos = yPos;
        this.board.GetBoard()[this.yPos][this.xPos] = this;
    }

    public boolean isDifferentColor(ChessPiece piece){
        if (piece == null) return false; // A null square has no color
        return this.isWhite != piece.isWhite;
    }

    public HashMap<int[], ChessPiece[]> GetValidSquares(){
        return null;
    };
}
