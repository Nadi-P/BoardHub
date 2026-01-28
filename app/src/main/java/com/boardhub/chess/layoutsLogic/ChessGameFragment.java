package com.boardhub.chess.layoutsLogic;

import static com.boardhub.chess.dataClasses.ChessUI.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.boardhub.R;
import com.boardhub.chess.dataClasses.*;
import com.boardhub.chess.pieces.ChessPiece;
import java.util.ArrayList;

public class ChessGameFragment extends Fragment {
    private TextView playerCapturedPiecesTextView,
            opponentCapturedPiecesTextView;
    private ChessPiece selectedPiece = null;
    private ArrayList<ChessMove> activeMoves = new ArrayList<>();
    private ImageButton[][] boardSquares = new ImageButton[8][8];
    private GridLayout chessGrid;
    private ChessPlayer player;
    private ChessGame game;

    public static ChessGameFragment newInstance(ChessPlayer player) {
        ChessGameFragment fragment = new ChessGameFragment();
        Bundle args = new Bundle();
        args.putSerializable("player", player);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            player = (ChessPlayer) getArguments().getSerializable("player");
            game = player.GetGame();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.chess_game_fragment, container, false);
        InitializeViews(root);
        InitializeBoard(root);
        UpdateBoardUI();
        return root;
    }
    private void InitializeViews(View rootView){
        chessGrid = rootView.findViewById(R.id.chess_grid);
        playerCapturedPiecesTextView = rootView.findViewById(R.id.playerCapturedPiecesTextView);
        opponentCapturedPiecesTextView = rootView.findViewById(R.id.opponentCapturedPiecesTextView);
    }

    private void InitializeBoard(View rootView) {
        chessGrid.removeAllViews();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ImageButton square = new ImageButton(getContext());
                square.setBackground(null);
                square.setPadding(0, 0, 0, 0);
                square.setScaleType(ImageView.ScaleType.FIT_CENTER);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(row, 1f);
                params.columnSpec = GridLayout.spec(col, 1f);
                square.setLayoutParams(params);

                // Logical mapping: Visual Row 0 is Logic Y 7
                final int logicX = col;
                final int logicY = 7 - row;

                square.setOnClickListener(v -> OnSquareClicked(logicX, logicY));
                boardSquares[row][col] = square;
                chessGrid.addView(square);
            }
        }
    }

    private void OnSquareClicked(int x, int y) {
        if (!player.GetIsWhite()){
            x = 7-x;
            y = 7-y;
        }
        ChessPiece clickedPiece = game.GetPieceAt(x, y);

        // 1. Attempting to move a selected piece
        if (selectedPiece != null) {
            ChessMove move = FindMoveInList(x, y);
            if (move != null) {
                ExecuteMove(move);
                return;
            }
        }

        // 2. Selecting a piece
        if (clickedPiece != null && clickedPiece.GetIsWhite() == game.GetIsWhiteTurn()) {
            if (selectedPiece == clickedPiece) {
                DeselectPiece();
            } else {
                SelectPiece(clickedPiece);
            }
        } else {
            DeselectPiece();
        }
        UpdateBoardUI();
    }

    private void SelectPiece(ChessPiece piece) {
        selectedPiece = piece;
        activeMoves = piece.GetMoves();
    }

    private void DeselectPiece() {
        selectedPiece = null;
        activeMoves.clear();
    }

    private void ExecuteMove(ChessMove move) {
        move.movedPiece.Capture(move);

        // Finalize turn
        DeselectPiece();
        UpdateCapturesUI();
        SwitchTimers();
        UpdateBoardUI();
        game.SetIsWhiteTurn(!game.GetIsWhiteTurn());
    }

    private void UpdateBoardUI() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int logicY = 7 - row;
                int logicX = col;

                if (!player.GetIsWhite()){
                    logicX = 7-logicX;
                    logicY = 7-logicY;
                }

                ImageButton square = boardSquares[row][col];
                ChessPiece piece = game.GetBoard()[logicY][logicX];

                // Set Base Background Color
                boolean isLight = (row + col) % 2 == 0;
                square.setBackgroundColor(isLight ? lightNormal : darkNormal);

                // Highlight Selected Piece
                if (selectedPiece != null && selectedPiece.GetXPos() == logicX && selectedPiece.GetYPos() == logicY) {
                    square.setBackgroundColor(isLight ? lightSelected : darkSelected);
                }

                // Highlight Available Moves
                if (IsMoveTarget(logicX, logicY)) {
                    square.setBackgroundColor(isLight ? lightHighlight : darkHighlight);
                }

                // Set Piece Icon
                if (piece != null) {
                    square.setImageResource(piece.GetImageResource());
                } else {
                    square.setImageResource(0);
                }
            }
        }
    }

    private ChessMove FindMoveInList(int x, int y) {
        for (ChessMove move : activeMoves) {
            if (move.targetX == x && move.targetY == y) return move;
        }
        return null;
    }

    private boolean IsMoveTarget(int x, int y) {
        return FindMoveInList(x, y) != null;
    }

    private void SwitchTimers() {
        // Implementation for starting/stopping CountdownTimers in ChessPlayer
    }

    private void UpdateCapturesUI() {
        ChessPlayer opponent = player.GetIsWhite() ? game.GetBlackPlayer() : game.GetWhitePlayer();
        String text1 = player.GetCapturesString();
        String text2 = opponent.GetCapturesString();

        playerCapturedPiecesTextView.setText("Captured: " + text1);
        opponentCapturedPiecesTextView.setText("Captured: " + text2);
        System.out.print(text1 + "\n" + text2);
    }
}