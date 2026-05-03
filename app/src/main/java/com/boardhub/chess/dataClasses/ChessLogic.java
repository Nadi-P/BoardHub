package com.boardhub.chess.dataClasses;

import android.graphics.Bitmap;

import com.boardhub.R;
import com.boardhub.chess.pieces.Bishop;
import com.boardhub.chess.pieces.ChessPiece;
import com.boardhub.chess.pieces.King;
import com.boardhub.chess.pieces.Knight;
import com.boardhub.chess.pieces.Pawn;
import com.boardhub.chess.pieces.Queen;
import com.boardhub.chess.pieces.Rook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface ChessLogic {
    interface Constants{
        int[] modesDurations = {
                5*1000,
                3*60*1000,
                10*60*1000,
                30*60*1000
        };

        int timerCountdownInterval = 1000;

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

        Map<String, Character> textIconsMap = Map.ofEntries(
                Map.entry("bPawn", blackPawnText),
                Map.entry("bRook", blackRookText),
                Map.entry("bKnight", blackKnightText),
                Map.entry("bBishop", blackBishopText),
                Map.entry("bQueen", blackQueenText),
                Map.entry("bKing", blackKingText),
                Map.entry("wPawn", whitePawnText),
                Map.entry("wRook", whiteRookText),
                Map.entry("wKnight", whiteKnightText),
                Map.entry("wBishop", whiteBishopText),
                Map.entry("wQueen", whiteQueenText),
                Map.entry("wKing", whiteKingText)
        );

        Map<String, Integer> FENtoIconMap = Map.ofEntries(
                Map.entry("r", R.drawable.chess_piece_black_rook),
                Map.entry("n", R.drawable.chess_piece_black_knight),
                Map.entry("b", R.drawable.chess_piece_black_bishop),
                Map.entry("q", R.drawable.chess_piece_black_queen),
                Map.entry("k", R.drawable.chess_piece_black_king),
                Map.entry("p", R.drawable.chess_piece_black_pawn),
                Map.entry("R", R.drawable.chess_piece_white_rook),
                Map.entry("N", R.drawable.chess_piece_white_knight),
                Map.entry("B", R.drawable.chess_piece_white_bishop),
                Map.entry("Q", R.drawable.chess_piece_white_queen),
                Map.entry("K", R.drawable.chess_piece_white_king),
                Map.entry("P", R.drawable.chess_piece_white_pawn),
                Map.entry("e", 0)
        );
        String[] pieceTypes = {"King", "Queen", "Rook", "Bishop", "Knight", "Pawn"};

        String[] FENChars = {"R", "N", "B", "Q", "K", "P", "r", "n", "b", "q", "k", "p", "e"};

        String[] winReasons = {"by Checkmate", "Opponent out of time", "by Resignation"};
        String[] drawReasons = {"Insufficient Materials", "Agreed Draw", "Repetition on Moves"};




        int[][] rookDirections = new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
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

    static ArrayList<ChessMove> FindMovesByBFS(
            ChessPiece piece, int[][] directions){
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        ChessGame board = piece.GetGame();

        for (int[] dir : directions) {
            int nextX = piece.GetXPos() + dir[0];
            int nextY = piece.GetYPos() + dir[1];

            while (IsPositionInBoard(nextX, nextY)) {
                ChessPiece pieceAtSquare = board.GetPieceAt(nextX, nextY);

                if (pieceAtSquare == null || piece.isDifferentColor(pieceAtSquare)) {
                    ChessMove move = new ChessMove(piece, pieceAtSquare, nextX, nextY);
                    move.Add(possibleMoves);
                    if (pieceAtSquare != null) break;
                }
                else break;

                nextX += dir[0];
                nextY += dir[1];
            }
        }

        return possibleMoves;
    }

    static ArrayList<ChessMove> FindMovesByLocations(
            ChessPiece piece, int[][] directions){
        ArrayList<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        ChessGame board = piece.GetGame();

        for (int[] dir : directions) {
            int nextX = piece.GetXPos() + dir[0];
            int nextY = piece.GetYPos() + dir[1];

            if (nextX >= 0 && nextX < 8 && nextY >= 0 && nextY < 8) {
                ChessPiece pieceAtSquare = board.GetPieceAt(nextX, nextY);

                if (pieceAtSquare == null || piece.isDifferentColor(pieceAtSquare)) {
                    ChessMove move = new ChessMove(piece, pieceAtSquare, nextX, nextY);
                    if (pieceAtSquare instanceof King) {
                        move.isCheckingOpponent = true;
                    }
                    move.Add(possibleMoves);
                }
            }
        }

        return possibleMoves;
    }

    static boolean IsPositionInBoard(int xPos, int yPos){
        return xPos >= 0 && xPos < 8 && yPos >= 0 && yPos < 8;
    }

    // Checks and Board Validations
    static boolean IsKingThreatened(King king, MockPiece[][] duplicate){
        int kingX = king.GetXPos(), kingY = king.GetYPos();
        boolean isWhiteTurn = king.GetIsWhite();

        // Check Sliding Pieces
        for (int[] d : Constants.queenDirections) {
            for (int i = 1; i < 8; i++) {
                int cx = kingX + (d[0] * i), cy = kingY + (d[1] * i);
                if (!IsPositionInBoard(cx, cy)) break;

                MockPiece p = duplicate[cy][cx]; // Corrected index [y][x]
                if (p != null) {
                    if (p.isWhite != isWhiteTurn) {
                        boolean isOrthogonal = (d[0] == 0 || d[1] == 0);
                        if (p.type == 5 || (isOrthogonal && p.type == 2) || (!isOrthogonal && p.type == 4)) return true;
                    }
                    break;
                }
            }
        }

        // Check Knights
        for (int[] m : Constants.knightDirections) {
            int nx = kingX + m[0], ny = kingY + m[1];
            if (IsPositionInBoard(nx, ny)) {
                MockPiece p = duplicate[ny][nx];
                if (p != null && p.isWhite != isWhiteTurn && p.type == 3) return true;
            }
        }

        // Check Pawns (Look for enemy pawns diagonal to king)
        int enemyY = isWhiteTurn ? kingY + 1 : kingY - 1;
        int[] enemyX = {kingX - 1, kingX + 1};
        for (int x : enemyX) {
            if (IsPositionInBoard(x, enemyY)) {
                MockPiece p = duplicate[enemyY][x];
                if (p != null && p.isWhite != isWhiteTurn && p.type == 1) return true;
            }
        }

        // Check King
        for (int[] d : Constants.kingDirections) {
            int kx = kingX + d[0], ky = kingY + d[1];
            if (IsPositionInBoard(kx, ky)) {
                MockPiece p = duplicate[ky][kx];
                if (p != null && p.isWhite != isWhiteTurn && p.type == 6) return true;
            }
        }

        return false;
    }

    static MockPiece[][] DuplicateBoard(ChessGame game){
        MockPiece[][] duplicate = new MockPiece[8][8];
        for (ChessPiece[] row : game.GetBoard()){
            for (ChessPiece piece : row){
                if (piece != null)
                    duplicate[piece.GetYPos()][piece.GetXPos()] = new MockPiece(piece);
            }
        }
        return duplicate;
    }

    class MockPiece {
        public int xPos;
        public int yPos;
        public boolean isWhite;
        public int type;

        public MockPiece(ChessPiece chessPiece) {
            this.xPos = chessPiece.GetXPos();
            this.yPos = chessPiece.GetYPos();
            this.isWhite = chessPiece.GetIsWhite();
            switch (chessPiece.getClass().getSimpleName()) {
                case "Pawn":
                    this.type = 1;
                    break;
                case "Rook":
                    this.type = 2;
                    break;
                case "Knight":
                    this.type = 3;
                    break;
                case "Bishop":
                    this.type = 4;
                    break;
                case "Queen":
                    this.type = 5;
                    break;
                case "King":
                    this.type = 6;
                    break;
                default:
                    this.type = 0;
                    break;
            }
        }
    }
    public static Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newEdge = Math.min(width, height); // Get the shorter side

        int xOffset = (width - newEdge) / 2;   // Calculate center start point
        int yOffset = (height - newEdge) / 2;

        return Bitmap.createBitmap(bitmap, xOffset, yOffset, newEdge, newEdge);
    }


}
