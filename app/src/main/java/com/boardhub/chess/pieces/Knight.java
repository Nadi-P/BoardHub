package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.ArrayList;
import java.util.HashMap;

public class Knight extends ChessPiece{
    public Knight(ChessPlayer player, int xPos, int yPos) {
        super(player, xPos, yPos);
        this.value = 3;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteKnightIcon : ChessLogic.Constants.blackKnightIcon;
    }
    @Override
    public ArrayList<ChessMove> GetMoves() {
        return ChessLogic.FindMovesByLocations(this, ChessLogic.Constants.knightDirections);
    }
}