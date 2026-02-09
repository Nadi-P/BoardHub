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
        System.out.println(this.toString() + " removed from game.");
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
