package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;

import java.util.ArrayList;

public class Bishop extends ChessPiece{
    public Bishop(ChessGame game, int xPos, int yPos, boolean isWhite){
        super(game, xPos, yPos, isWhite);
        this.FENid = (isWhite) ? ChessLogic.Constants.FENChars[2] : ChessLogic.Constants.FENChars[8];
        this.value = 3;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteBishopIcon : ChessLogic.Constants.blackBishopIcon;
    }
    @Override
    public ArrayList<ChessMove> GetMoves() {
        return ChessLogic.FindMovesByBFS(this, ChessLogic.Constants.bishopDirections);
    }


}
