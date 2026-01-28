package com.boardhub.chess.pieces;

import static com.boardhub.chess.dataClasses.ChessLogic.IsPositionInBoard;
import static com.boardhub.chess.dataClasses.ChessLogic.IsValidMove;

import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.ArrayList;
import java.util.HashMap;

public class Pawn extends ChessPiece{
    private boolean hasMoved;
    private boolean isValidEnPassant;

    public Pawn(ChessPlayer player, int xPos, int yPos){
        super(player, xPos, yPos);
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
        ChessGame game = this.player.GetGame();
        int pawnDir = this.isWhite ? 1 : -1;

        // 1. Forward Movement (Cannot capture)
        int nextX = this.xPos;
        int nextY = this.yPos + pawnDir;

        if (IsPositionInBoard(nextX, nextY) && game.GetPieceAt(nextX, nextY) == null) {
            ChessMove move1 = new ChessMove(this, null, nextX, nextY);
            if (IsValidMove(move1)) possibleMoves.add(move1);

            // Double move from starting rank
            int doubleY = this.yPos + (2 * pawnDir);
            if (!this.hasMoved && IsPositionInBoard(nextX, doubleY) && game.GetPieceAt(nextX, doubleY) == null) {
                ChessMove move2 = new ChessMove(this, null, nextX, doubleY);
                if (IsValidMove(move2)) possibleMoves.add(move2);
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
                    if (IsValidMove(move)) possibleMoves.add(move);
                }

                // En Passant check
                // Logic: Check if the square is empty, but there is an enemy pawn next to us
                // that just moved two squares.
                ChessPiece sidePiece = game.GetPieceAt(capX, this.yPos);
                if (target == null && sidePiece instanceof Pawn && this.isDifferentColor(sidePiece)) {
                    if (((Pawn) sidePiece).isValidEnPassant) {
                        ChessMove epMove = new ChessMove(this, sidePiece, capX, capY);
                        epMove.isEnPassant = true;
                        if (IsValidMove(epMove)) possibleMoves.add(epMove);
                    }
                }
            }
        }

        return possibleMoves;
    }

}
