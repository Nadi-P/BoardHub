package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.ArrayList;
import java.util.HashMap;

public class Queen extends ChessPiece {
    public Queen(ChessPlayer player, int xPos, int yPos) {
        super(player, xPos, yPos);
        this.value = 9;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteQueenIcon : ChessLogic.Constants.blackQueenIcon;
    }
    @Override
    public ArrayList<ChessMove> GetMoves() {
        return ChessLogic.FindMovesByBFS(this, ChessLogic.Constants.queenDirections);
    }
}