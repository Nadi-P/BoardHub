package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;

import java.util.ArrayList;

public class King extends ChessPiece{
    boolean isInCheck;
    boolean hasMoved;
    private Rook rookLeft, rookRight;

    public King(ChessGame game, int xPos, int yPos, boolean isWhite){
        super(game, xPos, yPos, isWhite);
        this.FENid = (isWhite) ? ChessLogic.Constants.FENChars[4] : ChessLogic.Constants.FENChars[10];
        this.value = 0;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whiteKingIcon : ChessLogic.Constants.blackKingIcon;

    }

    public boolean IsInCheck(){
        return this.isInCheck;
    }
    public void SetIsInCheck(boolean isInCheck){
        this.isInCheck = isInCheck;
    }
    public void SetRookLeft(Rook rook) {
        this.rookLeft = rook;
        if (rook != null){
            rook.SetKing(this);
            rook.SetIsLeftToKing(true);
        }
    }
    public void SetRookRight(Rook rook) {
        this.rookRight = rook;
        if (rook != null){
            rook.SetKing(this);
            rook.SetIsRightToKing(true);
        }
    }

    @Override
    public void MoveTo(int xPos, int yPos, boolean isVirtual){
        if (!isVirtual){
            this.hasMoved = true;
        }
        super.MoveTo(xPos, yPos, isVirtual);
    }
    @Override
    public ArrayList<ChessMove> GetMoves() {
        int kingRow = this.yPos;
        ArrayList<ChessMove> moves =  ChessLogic.FindMovesByLocations(this, ChessLogic.Constants.kingDirections);
        if (!hasMoved){
            ChessPiece[][] board = this.game.GetBoard();
            if (rookRight != null && !rookRight.HasMoved() &&
                    board[kingRow][5] == null &&
                    board[kingRow][6] == null){
                ChessMove castleRight = new ChessMove(this, null, 6, kingRow);
                castleRight.isRightCastling = true;
                castleRight.Add(moves);
            }
            if (rookLeft != null && !rookLeft.HasMoved() &&
                    board[kingRow][1] == null &&
                    board[kingRow][2] == null &&
                    board[kingRow][3] == null){
                ChessMove castleLeft = new ChessMove(this, null, 2, kingRow);
                castleLeft.isLeftCastling = true;
                castleLeft.Add(moves);
            }
        }
        return moves;
    }
}
