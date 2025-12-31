package com.boardhub.chess.dataClasses;

import com.boardhub.R;
import com.boardhub.chess.pieces.Bishop;
import com.boardhub.chess.pieces.ChessPiece;
import com.boardhub.chess.pieces.King;
import com.boardhub.chess.pieces.Knight;
import com.boardhub.chess.pieces.Pawn;
import com.boardhub.chess.pieces.Queen;
import com.boardhub.chess.pieces.Rook;

import java.util.HashMap;
import java.util.function.Function;

public interface ChessLogic {
    interface Constants{
        int classicModeDuration = 10*60*1000;
        int blitzModeDuration = 3*60*1000;
        int bulletModeDuration = 60*1000;

        int blackPawnIcon = R.drawable.chess_piece_black_pawn;
        int blackRookIcon = R.drawable.chess_piece_black_rook;
        int blackKnightIcon = R.drawable.chess_piece_black_knight;
        int blackBishopIcon = R.drawable.chess_piece_black_bishop;
        int blackQueenIcon = R.drawable.chess_piece_black_queen;
        int blackKingIcon = R.drawable.chess_piece_black_king;

        int whitePawnIcon = R.drawable.chess_piece_white_pawn;
        int whiteRookIcon = R.drawable.chess_piece_white_rook;
        int whiteKnightIcon = R.drawable.chess_piece_white_knight;
        int whiteBishopIcon = R.drawable.chess_piece_white_bishop;
        int whiteQueenIcon = R.drawable.chess_piece_white_queen;
        int whiteKingIcon = R.drawable.chess_piece_white_king;

        Character blackPawnText = '♟';
        Character blackRookText = '♜';
        Character blackKnightText = '♞';
        Character blackBishopText = '♝';
        Character blackQueenText = '♛';
        Character blackKingText = '♚';

        Character whitePawnText = '♙';
        Character whiteRookText = '♖';
        Character whiteKnightText = '♘';
        Character whiteBishopText = '♗';
        Character whiteQueenText = '♕';
        Character whiteKingText = '♔';

        int[][] rookDirections = new int[][]{
                {1, 0}, {-1, 0}, {1, 0}, {-1, 0}};
        int[][] bishopDirections = new int[][]{
                {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
        int[][] queenDirections = new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        int[][] knightDirections = new int[][]{
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2},
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1}};
        int[][] kingDirections = new int[][]{
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    }

    static boolean IsValidMove(int[] movePos, ChessPiece movedPiece,
                               ChessPiece capturedPiece){
        ChessGame chessGame = movedPiece.GetBoard();
        ChessPiece[][] board = chessGame.GetBoard();


        int startX = movedPiece.GetXPos();
        int startY = movedPiece.GetYPos();
        int targetX = movePos[0];
        int targetY = movePos[1];

        //include castling
        boolean isCastling = (movedPiece instanceof King) && Math.abs(targetX - startX) == 2;
        ChessPiece castleRook = null;
        int rookStartX = -1, rookEndX = -1;
        if (isCastling) {
            rookStartX = (targetX > startX) ? 7 : 0; // Kingside (7) or Queenside (0)
            rookEndX = (targetX > startX) ? 5 : 3;   // Behind the King
            castleRook = board[startY][rookStartX];
        }
        final ChessPiece castleRookFinal = castleRook;
        final int rookStartXFinal = rookStartX;

        Function<Boolean, Boolean> undo = (result) -> {
            movedPiece.MoveTo(startX, startY);
            if (capturedPiece != null) capturedPiece.AddToGame();
            if (isCastling && castleRookFinal != null) {
                castleRookFinal.MoveTo(rookStartXFinal, startY);
            }
            return result;
        };

        if (capturedPiece != null) capturedPiece.RemoveFromGame();
        movedPiece.PseudoMoveTo(targetX, targetY);
        if (isCastling && castleRookFinal != null) {
            castleRookFinal.MoveTo(rookEndX, startY); // MOVE THE ROOK IN SIMULATION
        }

        ChessPlayer player = movedPiece.GetPlayer();
        King king = player.GetKing();
        int kingX = king.GetXPos();
        int kingY = king.GetYPos();

        //Check for knights threats
        for (int[] deltas : Constants.knightDirections){
            int x = kingX + deltas[0];
            int y = kingY + deltas[1];
            if (ChessLogic.IsPositionInBoard(x, y)){
                ChessPiece piece = board[y][x];
                if (king.isDifferentColor(piece) && piece instanceof Knight)
                    return undo.apply(false);
            }
        }

        //Check for kings threats
        for (int[] deltas : Constants.kingDirections){
            int x = kingX + deltas[0];
            int y = kingY + deltas[1];
            if (ChessLogic.IsPositionInBoard(x, y)){
                ChessPiece piece = board[y][x];
                if (king.isDifferentColor(piece) && piece instanceof King)
                    return undo.apply(false);
            }
        }

        //Check for rooks & queens threats
        for (int[] deltas : Constants.rookDirections){
            int dx = deltas[0];
            int dy = deltas[1];
            int x = kingX + dx;
            int y = kingY + dy;
            ChessPiece piece = null;
            while (piece == null && ChessLogic.IsPositionInBoard(x, y)){
                piece = board[y][x];
                x += dx;
                y += dy;
            }
            if ((piece instanceof Rook || piece instanceof Queen) && king.isDifferentColor(piece))
                return undo.apply(false);
        }

        //Check for bishops & queens threats
        for (int[] deltas : Constants.bishopDirections){
            int dx = deltas[0];
            int dy = deltas[1];
            int x = kingX + dx;
            int y = kingY + dy;
            ChessPiece piece = null;
            while (piece == null && ChessLogic.IsPositionInBoard(x, y)){
                piece = board[y][x];
                x += dx;
                y += dy;
            }
            if ((piece instanceof Bishop || piece instanceof Queen) && king.isDifferentColor(piece))
                return undo.apply(false);
        }

        //Check for pawns threat
        int[][] pawnsDirections = player.GetIsWhite() ?
                new int[][]{{-1, -1},{1, -1}} :
                new int[][]{{-1, 1},{1, 1}};
        for (int[] deltas : pawnsDirections){
            int x = kingX + deltas[0];
            int y = kingY + deltas[1];
            if (ChessLogic.IsPositionInBoard(x, y)){
                ChessPiece piece = board[y][x];
                if (king.isDifferentColor(piece) && piece instanceof Pawn)
                    return undo.apply(false);
            }
        }

        return undo.apply(true);
    }

    static HashMap<int[], ChessPiece[]> FilterInvalidMoves(
            HashMap<int[], ChessPiece[]> moves) {
        moves.entrySet().removeIf(entry -> {
            int[] movePos = entry.getKey();
            ChessPiece movedPiece = entry.getValue()[0];
            ChessPiece capturedPiece = entry.getValue()[1];

            return !IsValidMove(movePos, movedPiece, capturedPiece);
        });
        return moves;
    }

    static HashMap<int[], ChessPiece[]> FindMovesByBFS(
            ChessPiece piece, int[][] directions){
        HashMap<int[], ChessPiece[]> validSquares = new HashMap<int[], ChessPiece[]>();

        ChessGame board = piece.GetPlayer().GetBoard();

        for (int[] dir : directions) {
            int nextX = piece.GetXPos() + dir[0];
            int nextY = piece.GetYPos() + dir[1];

            while (nextX >= 0 && nextX < 8 && nextY >= 0 && nextY < 8) {
                ChessPiece pieceAtSquare = board.GetPieceAt(nextX, nextY);

                if (pieceAtSquare == null) {
                    validSquares.put(new int[]{nextX, nextY}, new ChessPiece[]{piece, null});
                } else {
                    // Square is occupied
                    if (piece.isDifferentColor(pieceAtSquare)) {
                        validSquares.put(new int[]{nextX, nextY}, new ChessPiece[]{piece, pieceAtSquare});
                    }
                    break;
                }
                nextX += dir[0];
                nextY += dir[1];
            }
        }

        return validSquares;
    }

    static HashMap<int[], ChessPiece[]> FindMovesByLocations(
            ChessPiece piece, int[][] directions){
        HashMap<int[], ChessPiece[]> validSquares = new HashMap<int[], ChessPiece[]>();

        ChessGame board = piece.GetPlayer().GetBoard();

        for (int[] dir : directions) {
            int nextX = piece.GetXPos() + dir[0];
            int nextY = piece.GetYPos() + dir[1];

            if (nextX >= 0 && nextX < 8 && nextY >= 0 && nextY < 8) {
                ChessPiece pieceAtSquare = board.GetPieceAt(nextX, nextY);

                if (pieceAtSquare == null) {
                    validSquares.put(new int[]{nextX, nextY}, new ChessPiece[]{piece, null});
                }
                else if (piece.isDifferentColor(pieceAtSquare)) {
                    System.out.println(piece.GetIsWhite() + " " + pieceAtSquare.GetIsWhite());
                    validSquares.put(new int[]{nextX, nextY}, new ChessPiece[]{piece, pieceAtSquare});
                }
            }
        }

        return validSquares;
    }

    static boolean IsPositionInBoard(int xPos, int yPos){
        return xPos >= 0 && xPos < 8 && yPos >= 0 && yPos < 8;
    }

}
