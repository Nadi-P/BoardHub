package com.boardhub.chess.dataClasses;

import com.boardhub.chess.pieces.Bishop;
import com.boardhub.chess.pieces.ChessPiece;
import com.boardhub.chess.pieces.Knight;
import com.boardhub.chess.pieces.Queen;
import com.boardhub.chess.pieces.Rook;

import java.util.HashMap;
import java.util.Map;

public class ChessMove {
    public ChessGame game;
    public ChessPiece movedPiece;
    public ChessPiece capturedPiece;
    public int targetX;
    public int targetY;
    public boolean isRightCastling;
    public boolean isLeftCastling;
    public boolean isEnPassant;
    public boolean isPromotion;
    public int promotionPieceIndex;
    public long whiteTime;
    public long blackTime;
    public boolean isCheckingOpponent;

    public boolean isCheckmate;
    public boolean isStalemate;

    public ChessMove(ChessPiece movedPiece, ChessPiece capturedPiece, int targetX, int targetY) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.targetX = targetX;
        this.targetY = targetY;

        if (movedPiece != null) {
            this.game = movedPiece.GetGame();
        }
    }

    public ChessMove(ChessGame game, Map<String, Object> packet) {
        this.game = game;
        ChessPiece[][] board = game.GetBoard();

        // Use Long and then convert to int to avoid ClassCastException
        int xInitial = ((Long) packet.get("xInitial")).intValue();
        int yInitial = ((Long) packet.get("yInitial")).intValue();

        this.movedPiece = board[yInitial][xInitial];
        this.targetX = ((Long) packet.get("xTarget")).intValue();
        this.targetY = ((Long) packet.get("yTarget")).intValue();

        whiteTime = (long) packet.get("whiteTime");
        blackTime = (long) packet.get("blackTime");

        // Do the same for all other numerical fields
        this.promotionPieceIndex = ((Long) packet.get("promotionPieceIndex")).intValue();

        // Boolean fields do not need this conversion
        this.isRightCastling = (boolean) packet.get("isRightCastling");
        this.isLeftCastling = (boolean) packet.get("isLeftCastling");
        this.isEnPassant = (boolean) packet.get("isEnPassant");
        this.isPromotion = (boolean) packet.get("isPromotion");

        // Recalculate captured piece logic
        if (this.isEnPassant) {
            this.capturedPiece = game.GetPieceAt(targetX, yInitial);
        } else {
            this.capturedPiece = game.GetPieceAt(targetX, targetY);
        }
    }

    public void Execute(){
        if (movedPiece == null) return;

        ChessGame game = this.movedPiece.GetGame();
        ChessPiece[][] board = game.GetBoard();
        ChessPiece captured = this.capturedPiece;
        int x = targetX;
        int y = targetY;

        // 1. Handle En Passant
        if (isEnPassant && captured != null) {
            captured.RemoveFromGame(); // This clears the square next to the target
        }

        // 2. Handle Castling
        if (isRightCastling || isLeftCastling) {
            int rookX = isRightCastling ? 7 : 0;
            int rookTargetX = isRightCastling ? 5 : 3;
            ChessPiece rook = board[movedPiece.GetYPos()][rookX];

            movedPiece.MoveTo(x, y, false); // Move King
            if (rook != null) {
                rook.MoveTo(rookTargetX, y, false); // Move Rook
            }

            game.SetIsWhiteTurn(!movedPiece.GetIsWhite());
            game.GetMovesRecord().add(this);
            return;
        }

        if (isPromotion) {
            movedPiece.RemoveFromGame();
            switch (promotionPieceIndex) {
                case 0: new Queen(game, x, y, movedPiece.GetIsWhite()); break;
                case 1: new Knight(game, x, y, movedPiece.GetIsWhite()); break;
                case 2: new Rook(game, x, y, movedPiece.GetIsWhite()); break;
                case 3: new Bishop(game, x, y, movedPiece.GetIsWhite()); break;
            }
            game.SetIsWhiteTurn(!movedPiece.GetIsWhite());
            game.GetMovesRecord().add(this);
            return;
        }

        // 3. Standard Move/Capture
        if (captured != null) {
            if (captured instanceof Rook) {
                Rook rook = (Rook) captured;
                if (rook.GetKing() != null) {
                    if (rook.IsLeftToKing()) {
                        rook.GetKing().SetRookLeft(null);
                    } else if (rook.IsRightToKing()) {
                        rook.GetKing().SetRookRight(null);
                    }
                }
            }
            game.RecordCapture(captured);
            captured.RemoveFromGame();
        }
        movedPiece.MoveTo(x, y, false);
        game.SetIsWhiteTurn(!movedPiece.GetIsWhite());
        game.GetMovesRecord().add(this);
    }

    public Map<String, Object> FormatToPacket() {
        Map<String, Object> packet = new HashMap<>();

        packet.put("gameUID", game.GetUID());
        packet.put("boardFEN", game.GetBoardFEN());
        packet.put("movedPieceFEN", movedPiece.GetFENid());
        packet.put("targetPieceFEN", (capturedPiece != null) ? capturedPiece.GetFENid() : null);
        packet.put("xInitial", movedPiece.GetXPos());
        packet.put("yInitial", movedPiece.GetYPos());
        packet.put("xTarget", targetX);
        packet.put("yTarget", targetY);
        packet.put("whiteTime", game.GetTime(true));
        packet.put("blackTime", game.GetTime(false));
        packet.put("isWhiteTurn", !game.IsWhiteTurn());
        packet.put("isRightCastling", isRightCastling);
        packet.put("isLeftCastling", isLeftCastling);
        packet.put("isEnPassant", isEnPassant);
        packet.put("isPromotion", isPromotion);
        packet.put("promotionPieceIndex", promotionPieceIndex);

        return packet;
    }}
