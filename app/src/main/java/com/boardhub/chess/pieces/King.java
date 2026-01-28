package com.boardhub.chess.pieces;

import static com.boardhub.chess.dataClasses.ChessLogic.IsValidMove;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.ArrayList;
import java.util.HashMap;

public class King extends ChessPiece{
    boolean isInCheck;
    boolean hasMoved;
    private Rook rookLeft, rookRight;

    public King(ChessPlayer player, int xPos, int yPos){
        super(player, xPos, yPos);
        this.value = 0;
        player.SetKing(this);
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteKingIcon : ChessLogic.Constants.blackKingIcon;

    }

    public boolean GetIsInCheck(){
        return this.isInCheck;
    }
    public void SetIsInCheck(boolean isInCheck){
        this.isInCheck = isInCheck;
    }
    public void SetRookLeft(Rook rook1) { this.rookLeft = rook1; }
    public void SetRookRight(Rook rook2) { this.rookRight = rook2; }

    @Override
    public ArrayList<ChessMove> GetMoves() {
        int kingRow = this.isWhite ? 0 : 7;
        ArrayList<ChessMove> moves =  ChessLogic.FindMovesByLocations(this, ChessLogic.Constants.kingDirections);
        if (!hasMoved){
            ChessPiece[][] board = this.player.GetGame().GetBoard();
            if (!rookRight.HasMoved() &&
                    board[kingRow][5] == null &&
                    board[kingRow][6] == null){
                ChessMove castleRight = new ChessMove(this, null, 6, kingRow);
                castleRight.isRightCastling = true;
                if (IsValidMove(castleRight)) moves.add(castleRight);
            }
            if (!rookRight.HasMoved() &&
                    board[kingRow][1] == null &&
                    board[kingRow][2] == null &&
                    board[kingRow][3] == null){
                ChessMove castleLeft = new ChessMove(this, null, 2, kingRow);
                castleLeft.isLeftCastling = true;
                if (IsValidMove(castleLeft)) moves.add(castleLeft);
            }
        }
        return moves;
    }
}
