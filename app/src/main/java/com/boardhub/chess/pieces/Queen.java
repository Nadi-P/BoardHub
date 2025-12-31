package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.HashMap;

public class Queen extends ChessPiece {
    public Queen(ChessPlayer player, int xPos, int yPos) {
        super(player, xPos, yPos);
        this.value = 9;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteQueenIcon : ChessLogic.Constants.blackQueenIcon;
    }

    @Override
    public HashMap<int[], ChessPiece[]> GetValidSquares() {
        HashMap<int[], ChessPiece[]> moves = ChessLogic.FindMovesByBFS(this, ChessLogic.Constants.queenDirections);
        return ChessLogic.FilterInvalidMoves(moves);
    }
}