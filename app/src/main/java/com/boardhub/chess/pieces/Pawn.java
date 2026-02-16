package com.boardhub.chess.pieces;

import static com.boardhub.chess.dataClasses.ChessLogic.IsPositionInBoard;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;

import java.util.ArrayList;

public class Pawn extends ChessPiece{
    private boolean hasMoved;
    private boolean isValidEnPassant;

    public Pawn(ChessGame game, int xPos, int yPos, boolean isWhite){
        super(game, xPos, yPos, isWhite);
        this.FENid = (isWhite) ? ChessLogic.Constants.FENChars[5] : ChessLogic.Constants.FENChars[11];
        this.value = 1;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whitePawnIcon : ChessLogic.Constants.blackPawnIcon;
    }

    @Override
    public void MoveTo(int xPos, int yPos, boolean isVirtual){
        if (!isVirtual){
            isValidEnPassant = !this.hasMoved && Math.abs(yPos - this.yPos) == 2;
            this.hasMoved = true;
        }
        super.MoveTo(xPos, yPos, isVirtual);
    }

    @Override
    public ArrayList<ChessMove> GetMoves() {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        ChessGame game = this.GetGame();
        int pawnDir = this.isWhite ? 1 : -1;

        // 1. Forward Movement (Cannot capture)
        int nextX = this.xPos;
        int nextY = this.yPos + pawnDir;

        if (IsPositionInBoard(nextX, nextY) && game.GetPieceAt(nextX, nextY) == null) {
            ChessMove move1 = new ChessMove(this, null, nextX, nextY);
            if (nextY == 0 || nextY == 7) move1.isPromotion = true;
            move1.Add(possibleMoves);

            // Double move from starting rank
            int doubleY = this.yPos + (2 * pawnDir);
            if (!this.hasMoved && IsPositionInBoard(nextX, doubleY) && game.GetPieceAt(nextX, doubleY) == null) {
                ChessMove move2 = new ChessMove(this, null, nextX, doubleY);
                move2.Add(possibleMoves);
            }
        }

        // 2. Diagonal Captures (Must capture)
        int[][] captureOffsets = {{1, pawnDir}, {-1, pawnDir}};
        for (int[] offset : captureOffsets) {
            int capX = this.xPos + offset[0];
            int capY = this.yPos + offset[1];

            if (IsPositionInBoard(capX, capY)) {
                ChessPiece target = game.GetPieceAt(capX, capY);

                // Standard capture
                if (target != null && this.isDifferentColor(target)) {
                    ChessMove move = new ChessMove(this, target, capX, capY);
                    if (capY == 0 || capY == 7) move.isPromotion = true;
                    move.Add(possibleMoves);
                }

                // En Passant check
                // Logic: Check if the square is empty, but there is an enemy pawn next to us
                // that just moved two squares.
                ChessPiece sidePiece = game.GetPieceAt(capX, this.yPos);
                if (target == null && sidePiece instanceof Pawn && this.isDifferentColor(sidePiece)) {
                    if (((Pawn) sidePiece).isValidEnPassant) {
                        ChessMove epMove = new ChessMove(this, sidePiece, capX, capY);
                        epMove.isEnPassant = true;
                        epMove.Add(possibleMoves);
                    }
                }
            }
        }

        return possibleMoves;
    }

}
