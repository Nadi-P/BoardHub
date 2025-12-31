package com.boardhub.chess.pieces;

import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessPlayer;

import java.util.HashMap;

public class Pawn extends ChessPiece{
    private boolean hasMoved;
    private boolean isValidEnPassant;

    public Pawn(ChessPlayer player, int xPos, int yPos){
        super(player, xPos, yPos);
        this.value = 1;
        this.imageResource = (isWhite) ? ChessLogic.Constants.whitePawnIcon : ChessLogic.Constants.blackPawnIcon;
    }

    // --- Get Methods ---
    public boolean GetIsValidEnPassant(){
        return this.isValidEnPassant;
    }
    public boolean GetHasMoved(){
        return this.hasMoved;
    }

    // --- Set Methods ---
    public void SetIsValidEnPassant(boolean isValidEnPassant){
        this.isValidEnPassant = isValidEnPassant;
    }
    public void SetHasMoved(boolean hasMoved){
        this.hasMoved = hasMoved;
    }

    // --- Logic Methods ---
    @Override
    public HashMap<int[], ChessPiece[]> GetValidSquares(){
        HashMap<int[], ChessPiece[]> validSquares = new HashMap<int[], ChessPiece[]>();
        int x = this.xPos;
        int y = this.yPos;
        ChessPiece[][] board = this.player.GetBoard().GetBoard();

        // Determine direction: White moves -1 (up), Black moves +1 (down)
        int dir = this.isWhite ? -1 : 1;

        // --- 1. FORWARD MOVES ---
        int forwardY = y + dir;
        if (forwardY >= 0 && forwardY < 8 && board[forwardY][x] == null) {
            validSquares.put(new int[]{x, forwardY}, new ChessPiece[]{this, null});

            // Double move: check if on starting rank (6 for white, 1 for black)
            int startingRank = this.isWhite ? 6 : 1;
            int doubleForwardY = y + (2 * dir);
            if (y == startingRank && board[doubleForwardY][x] == null) {
                validSquares.put(new int[]{x, doubleForwardY}, new ChessPiece[]{this, null});
            }
        }

        // --- 2. DIAGONAL CAPTURES ---
        int[] sideOffsets = {-1, 1}; // Check left and right
        for (int dx : sideOffsets) {
            int targetX = x + dx;
            int targetY = y + dir;

            if (targetX >= 0 && targetX < 8 && targetY >= 0 && targetY < 8) {
                ChessPiece target = board[targetY][targetX];
                if (target != null && this.isDifferentColor(target)) {
                    validSquares.put(new int[]{targetX, targetY}, new ChessPiece[]{this, target});
                }

                // --- 3. EN PASSANT ---
                // Check the piece directly to the side of current position
                ChessPiece sidePiece = board[y][targetX];
                if (sidePiece instanceof Pawn && this.isDifferentColor(sidePiece)) {
                    if (((Pawn) sidePiece).GetIsValidEnPassant()) {
                        // Capture move goes to the empty square behind the enemy pawn
                        validSquares.put(new int[]{targetX, targetY}, new ChessPiece[]{this, sidePiece});
                    }
                }
            }
        }

        return ChessLogic.FilterInvalidMoves(validSquares);
    }

    @Override
    public void MoveTo(int xPos, int yPos) {
        this.isValidEnPassant = !this.hasMoved && 2 == Math.abs(yPos - this.yPos);
        super.MoveTo(xPos,yPos);
        this.hasMoved = true;

    }
}
