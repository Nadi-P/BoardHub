package com.boardhub.chess.dataClasses;

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
                60*1000,
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
        String[] pieceTypes = {"King", "Queen", "Rook", "Bishop", "Knight", "Pawn"};

        String[] FENChars = {"R", "N", "B", "Q", "K", "P", "r", "n", "b", "q", "k", "p", "e"};

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
                    if (IsValidMove(move)) possibleMoves.add(move);
                    if (pieceAtSquare != null){
                        break;
                    }
                }
                else {
                    break;
                }
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
                    if (IsValidMove(move)) possibleMoves.add(move);
                }
            }
        }

        return possibleMoves;
    }

    static boolean IsPositionInBoard(int xPos, int yPos){
        return xPos >= 0 && xPos < 8 && yPos >= 0 && yPos < 8;
    }

    // Checks and Board Validations

    static boolean IsValidMove(ChessMove move){
        ChessPiece movedPiece = move.movedPiece;
        ChessPiece capturedPiece = move.capturedPiece;
        ChessGame game = movedPiece.GetGame();

        int targetX = move.targetX;
        int targetY = move.targetY;
        int initialX = movedPiece.GetXPos();
        int initialY = movedPiece.GetYPos();
        boolean isWhiteTurn = movedPiece.GetIsWhite();

        // --- Special Simulation: Castling ---
        Rook castleRook = null;
        int rookInitX = -1, rookTargetX = -1;
        if (move.isRightCastling || move.isLeftCastling) {
            rookInitX = move.isRightCastling ? 7 : 0;
            rookTargetX = move.isRightCastling ? 5 : 3;
            castleRook = (Rook) game.GetBoard()[initialY][rookInitX];
            castleRook.MoveTo(rookTargetX, initialY, true);
        }

        // --- Special Simulation: En Passant ---
        if (move.isEnPassant) {
            game.GetBoard()[capturedPiece.GetYPos()][capturedPiece.GetXPos()] = null;
        }

        // 1. Simulate the move (Use true for isVirtual!)
        movedPiece.MoveTo(targetX, targetY, true);

        if (capturedPiece != null && !move.isEnPassant) {
            game.GetBoard()[targetY][targetX] = null;
        }

        movedPiece.MoveTo(targetX, targetY, true);

        // 2. Refresh the MockBoard AFTER the move is simulated
        MockPiece[][] duplicate = DuplicateBoard(game);
        King king = game.GetKing(isWhiteTurn);
        boolean inCheck = IsKingThreatened(king, duplicate);


        // 3. RESTORE EVERYTHING
        movedPiece.MoveTo(initialX, initialY, true);

        if (move.isEnPassant) {
            // Put the en-passant victim back where it was
            game.GetBoard()[capturedPiece.GetYPos()][capturedPiece.GetXPos()] = capturedPiece;
            game.GetBoard()[targetY][targetX] = null; // target square was empty
        } else {
            game.GetBoard()[targetY][targetX] = capturedPiece;
        }

        if (castleRook != null) {
            castleRook.MoveTo(rookInitX, initialY, true);
        }

        return !inCheck;
    }

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


}
