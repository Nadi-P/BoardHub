package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.HashMap;

public class Bishop extends ChessPiece{
    public Bishop(ChessPlayer player, int xPos, int yPos){
        super(player, xPos, yPos);
        this.value = 3;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteBishopIcon : ChessLogic.Constants.blackBishopIcon;
    }

    @Override
    public HashMap<int[], ChessPiece[]> GetValidSquares() {
        HashMap<int[], ChessPiece[]> moves = ChessLogic.FindMovesByBFS(this, ChessLogic.Constants.bishopDirections);
        return ChessLogic.FilterInvalidMoves(moves);
    }
}
