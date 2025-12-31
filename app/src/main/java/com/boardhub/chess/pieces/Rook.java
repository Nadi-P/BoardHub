package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.HashMap;

public class Rook extends ChessPiece{
    private boolean hasMoved;

    public Rook(ChessPlayer player, int xPos, int yPos){
        super(player, xPos, yPos);
        this.value = 5;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteRookIcon : ChessLogic.Constants.blackRookIcon;
    }

    public boolean GetHasMoved() {return this.hasMoved; }
    @Override
    public HashMap<int[], ChessPiece[]> GetValidSquares() {
        HashMap<int[], ChessPiece[]> moves = ChessLogic.FindMovesByBFS(this, ChessLogic.Constants.rookDirections);
        return ChessLogic.FilterInvalidMoves(moves);
    }
    @Override
    public void MoveTo(int xPos, int yPos) {
        super.MoveTo(xPos,yPos);
        this.hasMoved = true;
    }
}
