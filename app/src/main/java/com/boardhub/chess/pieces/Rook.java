package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;

import java.util.ArrayList;

public class Rook extends ChessPiece{
    private boolean hasMoved;
    private King king;
    private boolean isLeftToKing;
    private boolean isRightToKing;

    public Rook(ChessGame game, int xPos, int yPos, boolean isWhite){
        super(game, xPos, yPos, isWhite);
        this.FENid = (isWhite) ? ChessLogic.Constants.FENChars[0] : ChessLogic.Constants.FENChars[6];
        this.value = 5;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteRookIcon : ChessLogic.Constants.blackRookIcon;
    }
    public boolean HasMoved(){
        return this.hasMoved;
    }
    public void SetKing(King king){
        this.king = king;
    }
    public void SetIsLeftToKing(boolean isLeftToKing){
        this.isLeftToKing = isLeftToKing;
    }
    public void SetIsRightToKing(boolean isRightToKing){
        this.isRightToKing = isRightToKing;
    }
    public King GetKing(){
        return this.king;
    }
    public boolean IsLeftToKing(){
        return this.isLeftToKing;
    }
    public boolean IsRightToKing(){
        return this.isRightToKing;
    }

    @Override
    public void MoveTo(int xPos, int yPos, boolean isVirtual){
        if (!isVirtual) {
            this.hasMoved = true;
        }
        super.MoveTo(xPos, yPos, isVirtual);
    }
    @Override
    public ArrayList<ChessMove> GetMoves() {
        return ChessLogic.FindMovesByBFS(this, ChessLogic.Constants.rookDirections);
    }
}
