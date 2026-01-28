package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;

import java.util.ArrayList;

public class Queen extends ChessPiece {
    public Queen(ChessGame game, int xPos, int yPos, boolean isWhite){
        super(game, xPos, yPos, isWhite);
        this.FENid = (isWhite) ? ChessLogic.Constants.FENChars[3] : ChessLogic.Constants.FENChars[9];
        this.value = 9;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteQueenIcon : ChessLogic.Constants.blackQueenIcon;
    }
    @Override
    public ArrayList<ChessMove> GetMoves() {
        return ChessLogic.FindMovesByBFS(this, ChessLogic.Constants.queenDirections);
    }
}