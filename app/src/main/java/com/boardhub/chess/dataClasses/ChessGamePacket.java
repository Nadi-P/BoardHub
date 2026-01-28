package com.boardhub.chess.dataClasses;

import com.boardhub.chess.pieces.ChessPiece;

public class ChessGamePacket {
    String boardFEN;
    String movedPieceFEN;
    String targetPieceFEN;
    int xInitial;
    int yInitial;
    int xTarget;
    int yTarget;
    long whiteTime;
    long blackTime;
    boolean isWhiteTurn;

    public ChessGamePacket(ChessGame game, ChessMove move){
        ChessPiece movedPiece = move.movedPiece;
        ChessPiece capturedPiece = move.capturedPiece;
        StringBuilder boardBuilder = new StringBuilder();
        ChessPiece[][] board = game.GetBoard();

        for (int y = 7; y >= 0; y--){
            for (int x = 0; x < 8; x++){
                ChessPiece piece = board[y][x];
                if (piece != null) boardBuilder.append(piece.GetFENid());
                else boardBuilder.append(ChessLogic.Constants.FENChars[12]);
            }
        }

        boardFEN = boardBuilder.toString();

        if (capturedPiece != null) targetPieceFEN = capturedPiece.GetFENid();
        movedPieceFEN = move.movedPiece.GetFENid();

        xInitial = movedPiece.GetXPos();
        yInitial = movedPiece.GetYPos();
        xTarget = move.targetX;
        yTarget = move.targetY;

        whiteTime = game.GetWhitePlayer().GetTimeLeft();
        blackTime = game.GetWhitePlayer().GetTimeLeft();

        isWhiteTurn = game.GetIsWhiteTurn();

    }

}
