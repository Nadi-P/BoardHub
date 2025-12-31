package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.HashMap;

public class King extends ChessPiece{
    boolean isInCheck;
    boolean hasMoved;
    Rook rookLeft, rookRight;

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
    public HashMap<int[], ChessPiece[]> GetValidSquares() {
        HashMap<int[], ChessPiece[]> moves = ChessLogic.FindMovesByLocations(this, ChessLogic.Constants.kingDirections);
        if (!isInCheck){
            if (!rookLeft.GetHasMoved() && !this.hasMoved){
                int row = (this.isWhite) ? 7 : 0;
                ChessGame game = this.player.GetBoard();
                if (game.GetPieceAt(1, row) == null && game.GetPieceAt(2, row) == null && game.GetPieceAt(3, row) == null){
                    moves.put(new int[]{2, row}, new ChessPiece[]{this, null});
                }
            }
            if (!rookRight.GetHasMoved() && !this.hasMoved){
                int row = (this.isWhite) ? 7 : 0;
                ChessGame game = this.player.GetBoard();
                if (game.GetPieceAt(6, row) == null && game.GetPieceAt(5, row) == null){
                    moves.put(new int[]{6, row}, new ChessPiece[]{this, null});
                }
            }
        }
        return ChessLogic.FilterInvalidMoves(moves);
    }

    @Override
    public void MoveTo(int xPos, int yPos) {
        // Castling logic only for REAL moves
        if (this.xPos - xPos == 2) {
            rookLeft.MoveTo(xPos + 1, yPos);
        } else if (this.xPos - xPos == -2) {
            rookRight.MoveTo(xPos - 1, yPos);
        }
        super.MoveTo(xPos, yPos);
        this.hasMoved = true; // Permanent flag
    }
}
