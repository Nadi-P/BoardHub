package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.ArrayList;
import java.util.HashMap;

public class Rook extends ChessPiece{
    private boolean hasMoved;

    public Rook(ChessPlayer player, int xPos, int yPos){
        super(player, xPos, yPos);
        this.value = 5;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteRookIcon : ChessLogic.Constants.blackRookIcon;
    }
    public boolean HasMoved(){
        return this.hasMoved;
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
