package com.boardhub.chess.dataClasses;

import static com.boardhub.chess.dataClasses.ChessLogic.DuplicateBoard;
import static com.boardhub.chess.dataClasses.ChessLogic.IsKingThreatened;

import com.boardhub.chess.pieces.Bishop;
import com.boardhub.chess.pieces.ChessPiece;
import com.boardhub.chess.pieces.Knight;
import com.boardhub.chess.pieces.Queen;
import com.boardhub.chess.pieces.Rook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChessMove {
    public ChessGame game;
    public ChessPiece movedPiece, capturedPiece;
    public int targetX, targetY, promotionPieceIndex;
    public long whiteTime, blackTime;
    public boolean
            isWhiteTurn,
            isRightCastling,
            isLeftCastling,
            isEnPassant,
            isPromotion,
            isCheckingOpponent,
            isCheckmate,
            isStalemate,
            isDraw,
            isResignation,
            isOutOfTime,
            isDrawOffer,
            isDrawAccept,
            isDrawDecline,
            isRepetition;
    public String boardFen;
    public int initialX, initialY;


    // normal
    public ChessMove(ChessPiece movedPiece, ChessPiece capturedPiece, int targetX, int targetY) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.targetX = targetX;
        this.targetY = targetY;

        if (movedPiece != null) {
            this.game = movedPiece.GetGame();
            this.isWhiteTurn = movedPiece.GetIsWhite();
            this.initialX = movedPiece.GetXPos();
            this.initialY = movedPiece.GetYPos();
        }
    }
    // load from packets
    public ChessMove(ChessGame game, int gameEndIndex) {
        this.game = game;
        isWhiteTurn = game.IsWhiteTurn();
        initialX = -1;
        initialY = -1;
        targetX = -1;
        targetY = -1;

        switch (gameEndIndex) {
            case 0:
                isCheckmate = true;
                break;
            case 1:
                isStalemate = true;
                break;
            case 2:
                isDraw = true;
                break;
            case 3:
                isResignation = true;
                break;
            case 4:
                isOutOfTime = true;
                break;
            case 5:
                isDrawOffer = true;
                break;
            case 6:
                isDrawAccept = true;
                break;
            case 7:
                isDrawDecline = true;
                break;
            default:
                break;
        }
    }

    public ChessMove(ChessGame game, Map<String, Object> packet) {
        this.game = game;
        ChessPiece[][] board = game.GetBoard();

        // 1. Check for nulls before converting Long to int
        Object xInitObj = packet.get("xInitial");
        Object yInitObj = packet.get("yInitial");

        // If these are null, it's an initialization packet, not a move packet
        int xInitial = (xInitObj != null) ? ((Long) xInitObj).intValue() : -1;
        int yInitial = (yInitObj != null) ? ((Long) yInitObj).intValue() : -1;

        this.initialX = xInitial;
        this.initialY = yInitial;
        this.movedPiece = (yInitial >= 0 && xInitial >= 0) ? board[yInitial][xInitial] : null;

        Object xTargetObj = packet.get("xTarget");
        Object yTargetObj = packet.get("yTarget");
        this.targetX = (xTargetObj != null) ? ((Long) xTargetObj).intValue() : -1;
        this.targetY = (yTargetObj != null) ? ((Long) yTargetObj).intValue() : -1;

        // 2. Safely parse times
        this.whiteTime = packet.containsKey("whiteTime") ? (long) packet.get("whiteTime") : 0;
        this.blackTime = packet.containsKey("blackTime") ? (long) packet.get("blackTime") : 0;

        Object promoObj = packet.get("promotionPieceIndex");
        this.promotionPieceIndex = (promoObj != null) ? ((Long) promoObj).intValue() : 0;

        // 3. Use a helper method for safe boolean unboxing
        this.isWhiteTurn = safeBool(packet, "isWhiteTurn", true);
        this.isRightCastling = safeBool(packet, "isRightCastling", false);
        this.isLeftCastling = safeBool(packet, "isLeftCastling", false);
        this.isEnPassant = safeBool(packet, "isEnPassant", false);
        this.isPromotion = safeBool(packet, "isPromotion", false);
        this.isCheckingOpponent = safeBool(packet, "isCheckingOpponent", false);
        this.isCheckmate = safeBool(packet, "isCheckmate", false);
        this.isStalemate = safeBool(packet, "isStalemate", false);
        this.isDraw = safeBool(packet, "isDraw", false);
        this.isResignation = safeBool(packet, "isResignation", false);
        this.isOutOfTime = safeBool(packet, "isOutOfTime", false);
        this.isDrawOffer = safeBool(packet, "isDrawOffer", false);
        this.isDrawAccept = safeBool(packet, "isDrawAccept", false);
        this.isDrawDecline = safeBool(packet, "isDrawDecline", false);
        this.isRepetition = safeBool(packet, "isRepetition", false);

        this.boardFen = (String) packet.get("boardFEN");

        // 4. Only calculate captured piece if we actually have a valid target
        if (this.targetX != -1) {
            if (this.isEnPassant) {
                this.capturedPiece = game.GetPieceAt(targetX, yInitial);
            } else {
                this.capturedPiece = game.GetPieceAt(targetX, targetY);
            }
        }
    }
    private boolean safeBool(Map<String, Object> packet, String key, boolean defaultValue) {
        Object val = packet.get(key);
        return (val instanceof Boolean) ? (Boolean) val : defaultValue;
    }
    public Map<String, Object> FormatToPacket() {
        Map<String, Object> packet = new HashMap<>();

        packet.put("gameUID", game.GetUID());
        packet.put("movedPieceFEN", movedPiece.GetFENid());
        packet.put("targetPieceFEN", (capturedPiece != null) ? capturedPiece.GetFENid() : null);
        packet.put("xInitial", movedPiece.GetXPos());
        packet.put("yInitial", movedPiece.GetYPos());
        packet.put("xTarget", targetX);
        packet.put("yTarget", targetY);
        packet.put("whiteTime", game.GetTime(true));
        packet.put("blackTime", game.GetTime(false));
        packet.put("isWhiteTurn", isWhiteTurn);
        packet.put("isRightCastling", isRightCastling);
        packet.put("isLeftCastling", isLeftCastling);
        packet.put("isEnPassant", isEnPassant);
        packet.put("isPromotion", isPromotion);
        packet.put("promotionPieceIndex", promotionPieceIndex);
        packet.put("isCheckingOpponent", isCheckingOpponent);
        packet.put("isCheckmate", isCheckmate);
        packet.put("isStalemate", isStalemate);
        packet.put("isDraw", isDraw);
        packet.put("isResignation", isResignation);
        packet.put("isOutOfTime", isOutOfTime);
        packet.put("isDrawOffer", isDrawOffer);
        packet.put("isDrawAccept", isDrawAccept);
        packet.put("isDrawDecline", isDrawDecline);
        packet.put("isRepetition", isRepetition);

        return packet;
    }

    public void Execute() {
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

            game.SetIsWhiteTurn(!isWhiteTurn);
            this.boardFen = game.GetBoardFEN();
            game.GetMovesRecord().add(this);
            return;
        }

        if (isPromotion) {
            movedPiece.RemoveFromGame();
            switch (promotionPieceIndex) {
                case 0:
                    new Queen(game, x, y, movedPiece.GetIsWhite());
                    break;
                case 1:
                    new Knight(game, x, y, movedPiece.GetIsWhite());
                    break;
                case 2:
                    new Rook(game, x, y, movedPiece.GetIsWhite());
                    break;
                case 3:
                    new Bishop(game, x, y, movedPiece.GetIsWhite());
                    break;
            }
            game.SetIsWhiteTurn(!isWhiteTurn);
            this.boardFen = game.GetBoardFEN();
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
        game.SetIsWhiteTurn(!isWhiteTurn);
        this.boardFen = game.GetBoardFEN();
        game.GetMovesRecord().add(this);
    }
    public Rook ExecuteVirtually() {
        ChessMove move = this;
        ChessPiece movedPiece = move.movedPiece;
        ChessPiece capturedPiece = move.capturedPiece;
        ChessGame game = movedPiece.GetGame();
        int initialY = movedPiece.GetYPos();

        // 1. Special Simulation: Castling
        Rook castleRook = null;
        if (move.isRightCastling || move.isLeftCastling) {
            int rookInitX = move.isRightCastling ? 7 : 0;
            int rookTargetX = move.isRightCastling ? 5 : 3;
            castleRook = (Rook) game.GetBoard()[initialY][rookInitX];
            if (castleRook != null) {
                castleRook.MoveTo(rookTargetX, initialY, true);
            }
        }

        // 2. Special Simulation: En Passant
        if (move.isEnPassant && capturedPiece != null) {
            game.GetBoard()[capturedPiece.GetYPos()][capturedPiece.GetXPos()] = null;
        }

        // 3. Move the piece
        if (capturedPiece != null && !move.isEnPassant) {
            game.GetBoard()[move.targetY][move.targetX] = null;
        }
        movedPiece.MoveTo(move.targetX, move.targetY, true);

        return castleRook;
    }
    public void UnExecuteVirtually(Rook castleRook, int initialX, int initialY) {
        ChessMove move = this;
        ChessPiece movedPiece = move.movedPiece;
        ChessPiece capturedPiece = move.capturedPiece;
        ChessGame game = movedPiece.GetGame();

        // 1. Move the main piece back
        movedPiece.MoveTo(initialX, initialY, true);

        // 2. Restore captured piece / square state
        if (move.isEnPassant && capturedPiece != null) {
            game.GetBoard()[capturedPiece.GetYPos()][capturedPiece.GetXPos()] = capturedPiece;
            game.GetBoard()[move.targetY][move.targetX] = null;
        } else {
            game.GetBoard()[move.targetY][move.targetX] = capturedPiece;
        }

        // 3. Restore Castling Rook
        if (castleRook != null) {
            int rookInitX = move.isRightCastling ? 7 : 0;
            castleRook.MoveTo(rookInitX, initialY, true);
        }
    }

    public boolean IsValid(){
        ChessMove move = this;
        int initialX = move.movedPiece.GetXPos();
        int initialY = move.movedPiece.GetYPos();
        ChessGame game = move.movedPiece.GetGame();

        // PART 1: Perform the move
        Rook castleRook = ExecuteVirtually();

        // PART 2: Reach conclusions
        ChessLogic.MockPiece[][] duplicate = DuplicateBoard(game);
        boolean resultsInSelfCheck = IsKingThreatened(game.GetKing(move.movedPiece.GetIsWhite()), duplicate);
        move.isCheckingOpponent = IsKingThreatened(game.GetKing(!move.movedPiece.GetIsWhite()), duplicate);

        // PART 3: Undo the move
        UnExecuteVirtually(castleRook, initialX, initialY);

        return !resultsInSelfCheck;
    }
    public void CheckIsCheckmate() {
        if (movedPiece == null) return;
        ChessMove move = this;
        ChessGame game = move.game;
        boolean isWhiteTurn = move.isWhiteTurn;
        int initialX = move.movedPiece.GetXPos();
        int initialY = move.movedPiece.GetYPos();
        ArrayList<ChessPiece> opponentPieces = game.GetPieces(!isWhiteTurn);
        boolean opponentHasLegalMoves = false;

        Rook castleRook = ExecuteVirtually();

        // PART 2: Reach conclusions
        for (ChessPiece p : opponentPieces) {
            if (game.GetBoard()[p.GetYPos()][p.GetXPos()] == p && !p.GetMoves().isEmpty()) {
                opponentHasLegalMoves = true; break;
            }
        }

        if (!opponentHasLegalMoves) {
            if (move.isCheckingOpponent) move.isCheckmate = true;
            else move.isStalemate = true;
        }

        // PART 3: Undo the move
        UnExecuteVirtually(castleRook, initialX, initialY);
    }
    public void Add(ArrayList<ChessMove> moves){
        if (moves == null) return;
        if (this.IsValid()) {
            moves.add(this);
        }
    }
}
