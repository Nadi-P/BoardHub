package com.boardhub.chess.layoutsLogic;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.boardhub.R;
import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessPlayer;
import com.boardhub.chess.pieces.ChessPiece;

import java.util.HashMap;
import java.util.Set;

public class ChessGameFragment extends Fragment {
    private final int LIGHT_SQUARE = Color.parseColor("#eeeed2");
    private final int DARK_SQUARE = Color.parseColor("#769656");
    // The yellow highlight for the piece you just picked up
    private final int HIGHLIGHT_SELECTED = Color.parseColor("#F5F682");

    // Cyan highlights for the potential destination squares
    private final int HIGHLIGHT_CYAN_LIGHT = Color.parseColor("#11B5E4"); // Brighter for light squares
    private final int HIGHLIGHT_CYAN_DARK = Color.parseColor("#0E94BA");  // Deeper for dark squares


    private ChessPiece selectedPiece = null;
    private HashMap<int[], ChessPiece[]> activeMoves = new HashMap<>();

    private ImageButton[][] boardSquares = new ImageButton[8][8];
    private GridLayout chessGrid;

    private static final String ARG_PLAYER = "player";
    private ChessPlayer player;
    private ChessGame game;

    public ChessGameFragment() {
    }

    public static ChessGameFragment newInstance(ChessPlayer player) {
        ChessGameFragment fragment = new ChessGameFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAYER, player);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            player = (ChessPlayer) getArguments().getSerializable(ARG_PLAYER);
            game = player.GetBoard();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.chess_game_fragment, container, false);
        setupBoard(root);
        LoadBoardPosition();
        return root;
    }

    private void setupBoard(View rootView) {
        chessGrid = rootView.findViewById(R.id.chess_grid);

        // Clear any existing views if this is called multiple times
        chessGrid.removeAllViews();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ImageButton square = new ImageButton(getContext());

                // 1. CRITICAL: Remove default Android button styling (shadows/internal padding)
                square.setBackground(null);
                square.setPadding(0, 0, 0, 0);
                square.setScaleType(ImageView.ScaleType.FIT_CENTER);

                // 2. Use GridLayout Weights to fill the 1:1 Square perfectly
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();

                // width/height 0 tells the layout to use weights instead of fixed pixels
                params.width = 0;
                params.height = 0;

                // rowSpec(index, weight) - weight 1f ensures equal distribution
                params.rowSpec = GridLayout.spec(row, 1f);
                params.columnSpec = GridLayout.spec(col, 1f);

                // Ensure no accidental margins
                params.setMargins(0, 0, 0, 0);
                square.setLayoutParams(params);

                // 3. Coloring Logic
                if ((row + col) % 2 == 0) {
                    square.setBackgroundColor(LIGHT_SQUARE);
                } else {
                    square.setBackgroundColor(DARK_SQUARE);
                }

                // 4. Tagging and Click Listeners
                final int xPos = col;
                final int yPos = row;
                square.setTag(new int[]{row, col});
                square.setOnClickListener(v -> onSquareClicked(xPos, yPos));

                // 5. Add to UI and Reference Array
                chessGrid.addView(square);
                boardSquares[row][col] = square;
            }
        }
    }

    private void onSquareClicked(int x, int y){
        ChessPiece clickedPiece = game.GetPieceAt(x, y);

        if (selectedPiece != null) {
            //if pressed on an empty square
            if (clickedPiece == null) {
                int[] key = findPosInMap(activeMoves.keySet(), x, y);
                if (key != null){ //if its in the legal moves, execute the move
                    ChessPiece[] pieces = activeMoves.get(key);
                    ExecuteMove(x, y, pieces[0], pieces[1]);
                } else {
                    LoadBoardPosition();
                }
                selectedPiece = null;
                activeMoves = null;
            }

            //if pressed an opponent piece
            else if (clickedPiece.GetIsWhite() != game.GetIsWhiteTurn()) {
                int[] key = findPosInMap(activeMoves.keySet(), x, y);
                if (key != null){ //if its in the legal moves, execute the move
                    ChessPiece[] pieces = activeMoves.get(key);
                    ExecuteMove(x, y, pieces[0], pieces[1]);
                } else {
                    LoadBoardPosition();
                }
                selectedPiece = null;
                activeMoves = null;
            }

            //if pressed on same square
            else if (clickedPiece == selectedPiece) {
                selectedPiece = null;
                activeMoves = null;
                LoadBoardPosition();
            }

            //if pressed on a player piece
            else {
                LoadBoardPosition();
                selectedPiece = clickedPiece;
                activeMoves = selectedPiece.GetValidSquares();

                for (int[] coords : activeMoves.keySet()) {
                    int targetX = coords[0];
                    int targetY = coords[1];
                    ImageButton square = boardSquares[targetY][targetX];

                    // Apply two-tone logic: Check if the destination square is a light or dark square
                    if ((targetX + targetY) % 2 == 0) {
                        square.setBackgroundColor(HIGHLIGHT_CYAN_LIGHT);
                    } else {
                        square.setBackgroundColor(HIGHLIGHT_CYAN_DARK);
                    }
                }
            }
        }
        else {
            if (clickedPiece != null && clickedPiece.GetIsWhite() == game.GetIsWhiteTurn()) {
                LoadBoardPosition();
                selectedPiece = clickedPiece;
                activeMoves = selectedPiece.GetValidSquares();

                for (int[] coords : activeMoves.keySet()) {
                    int targetX = coords[0];
                    int targetY = coords[1];
                    ImageButton square = boardSquares[targetY][targetX];

                    // Apply two-tone logic: Check if the destination square is a light or dark square
                    if ((targetX + targetY) % 2 == 0) {
                        square.setBackgroundColor(HIGHLIGHT_CYAN_LIGHT);
                    } else {
                        square.setBackgroundColor(HIGHLIGHT_CYAN_DARK);
                    }
                }
            }
        }
    }

    private int[] findPosInMap(Set<int[]> set, int x, int y) {
        for (int[] pos : set) {
            if (pos[0] == x && pos[1] == y) return pos;
        }
        return null;
    }

    private void ExecuteMove(int xPos, int yPos, ChessPiece movedPiece, ChessPiece capturedPiece){
        if (capturedPiece != null) capturedPiece.RemoveFromGame();
        movedPiece.MoveTo(xPos, yPos);
        game.SetIsWhiteTurn(!game.GetIsWhiteTurn());
        LoadBoardPosition();
        Toast.makeText(requireContext(), "Successful Move!", Toast.LENGTH_SHORT).show();
    }

    private void LoadBoardPosition(){
        for (ChessPiece[] row : player.GetBoard().GetBoard()){
            for (ChessPiece piece : row){
                if (piece != null){
                    int xPos = piece.GetXPos();
                    int yPos = piece.GetYPos();
                    ImageButton square = boardSquares[yPos][xPos];
                    square.setImageResource(piece.GetImageResource());
                }
            }
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ImageButton square = boardSquares[row][col];

                if ((row + col) % 2 == 0) {
                    square.setBackgroundColor(LIGHT_SQUARE);
                } else {
                    square.setBackgroundColor(DARK_SQUARE);
                }

                ChessPiece p = player.GetBoard().GetBoard()[row][col];
                if (p != null) {
                    square.setImageResource(p.GetImageResource());
                } else {
                    square.setImageResource(0);
                }
            }
        }
    }
}