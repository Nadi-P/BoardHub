package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;

import java.util.ArrayList;

public class Knight extends ChessPiece{
    public Knight(ChessGame game, int xPos, int yPos, boolean isWhite){
        super(game, xPos, yPos, isWhite);
        this.FENid = (isWhite) ? ChessLogic.Constants.FENChars[1] : ChessLogic.Constants.FENChars[7];
        this.value = 3;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteKnightIcon : ChessLogic.Constants.blackKnightIcon;
    }
    @Override
    public ArrayList<ChessMove> GetMoves() {
        return ChessLogic.FindMovesByLocations(this, ChessLogic.Constants.knightDirections);
    }
}