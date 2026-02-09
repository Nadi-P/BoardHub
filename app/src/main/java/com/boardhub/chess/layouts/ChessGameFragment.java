package com.boardhub.chess.layouts;

import static com.boardhub.chess.dataClasses.ChessUI.*;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import com.boardhub.R;
import com.boardhub.chess.dataClasses.*;
import com.boardhub.chess.pieces.ChessPiece;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Map;

public class ChessGameFragment extends Fragment {
    private ListenerRegistration gameListener;

    private boolean isWhite;
    private ChessGame game;
    private boolean isSingleplayer;

    private final android.os.Handler timerHandler = new android.os.Handler();
    private Runnable timerRunnable;
    private TextView playerTimeTextView, opponentTimeTextView;

    private TextView playerCapturedPiecesTextView,
            opponentCapturedPiecesTextView;
    private GridLayout promotionsMenu;
    private ImageButton promotionOptionQueen,
            promotionOptionKnight,
            promotionOptionRook,
            promotionOptionBishop,
            promotionOptionCancel;

    private ChessPiece selectedPiece = null;
    private ArrayList<ChessMove> activeMoves = new ArrayList<>();
    private ImageButton[][] boardSquares = new ImageButton[8][8];
    private GridLayout chessGrid;

    public static ChessGameFragment newInstance(ChessGame game, boolean isSingleplayer) {
        ChessGameFragment fragment = new ChessGameFragment();
        Bundle args = new Bundle();
        args.putSerializable("game", game);
        args.putSerializable("isSingleplayer", isSingleplayer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            game = (ChessGame) getArguments().getSerializable("game");
            isWhite = game.GetAssignedIsWhite();
            isSingleplayer = (boolean) getArguments().getSerializable("isSingleplayer");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.chess_game_fragment, container, false);
        InitializeViews(root);
        InitializeBoard();
        UpdateBoardUI();

        if (!isSingleplayer) {
            InitializeGameListener();
        }

        SwitchTimers();
        SwitchTimers();

        return root;
    }

    // --- Initializations ---

    private void InitializeViews(View rootView){
        chessGrid = rootView.findViewById(R.id.chess_grid);
        playerCapturedPiecesTextView = rootView.findViewById(R.id.playerCapturedPiecesTextView);
        opponentCapturedPiecesTextView = rootView.findViewById(R.id.opponentCapturedPiecesTextView);
        promotionsMenu = rootView.findViewById(R.id.promotionsMenu_grid);
        playerTimeTextView = rootView.findViewById(R.id.playerTimeView);
        opponentTimeTextView = rootView.findViewById(R.id.opponentTimeView);
    }

    private void InitializeBoard() {
        chessGrid.removeAllViews();
        int padding = promotionMenuPadding;
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

                square.setOnClickListener(v -> OnSquareClicked(
                        (isWhite) ? logicX : 7 - logicX,
                        (isWhite) ? logicY : 7 - logicY));
                boardSquares[row][col] = square;
                chessGrid.addView(square);
            }
        }

        ImageButton[] crownOptions = new ImageButton[4];
        Context context = getContext();
        for (int i = 0; i < 4; i++){
            ImageButton button = new ImageButton(context);
            button.setBackground(null);
            button.setPadding(padding, padding, padding, padding);
            button.setBackgroundColor(ChessUI.promotionMenuBackground);
            button.setScaleType(ImageView.ScaleType.FIT_CENTER);

            crownOptions[i] = button;
        }

        ImageButton cancelButton = new ImageButton(context);
        cancelButton.setBackground(null);
        cancelButton.setImageResource(R.drawable.cancel_mark_ic);
        cancelButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        cancelButton.setBackgroundColor(ChessUI.promotionMenuBackground);
        cancelButton.setPadding(padding, padding, padding, padding);
        cancelButton.setOnClickListener(v -> {
            promotionsMenu.setVisibility(View.GONE);
            DeselectPiece();
            UpdateBoardUI();
        });

        promotionOptionQueen = crownOptions[0];
        promotionOptionKnight = crownOptions[1];
        promotionOptionRook = crownOptions[2];
        promotionOptionBishop = crownOptions[3];
        promotionOptionCancel = cancelButton;
    }

    private void InitializeGameListener() {
        gameListener = ChessDBI.ListenToGame(game.GetUID(), (snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) return;

            Map<String, Object> packet = snapshot.getData();
            if (packet == null) return;

            boolean isWhiteTurnInPacket = (boolean) packet.get("isWhiteTurn");

            if (isWhiteTurnInPacket != game.IsWhiteTurn()) {
                ChessMove receivedMove = new ChessMove(game, packet);
                receivedMove.Execute();

                game.SetTime((long) packet.get("whiteTime"), true);
                game.SetTime((long) packet.get("blackTime"), false);

                UpdateCapturesUI();
                SwitchTimers();
                DeselectPiece();
                UpdateBoardUI();
            }
        });
    }

    // --- Operations ---

    private void SelectPiece(ChessPiece piece) {
        ArrayList<ChessMove> moves = piece.GetMoves();
        if (moves == null || moves.isEmpty()) return;
        activeMoves = moves;
        selectedPiece = piece;
    }

    private void DeselectPiece() {
        selectedPiece = null;
        activeMoves.clear();
    }

    private void ExecuteMove(ChessMove move) {
        if (!isSingleplayer){
            ChessDBI.SaveMove(move);
        }
        else {
            move.Execute();
            UpdateCapturesUI();
            DeselectPiece();
            SwitchTimers();
            UpdateBoardUI();
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
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        // 2. Your Color Logic
        int activeTimerBackgroundColor = (game.IsWhiteTurn()) ? ChessUI.white : ChessUI.black;
        int activeTimerTextColor = (game.IsWhiteTurn()) ? ChessUI.black : ChessUI.white;
        int disabledBackground = ChessUI.disabledBackground;

        TextView activeTimer = (isWhite == game.IsWhiteTurn()) ? playerTimeTextView : opponentTimeTextView;
        TextView inactiveTimer = (isWhite == game.IsWhiteTurn()) ? opponentTimeTextView : playerTimeTextView;

        activeTimer.setBackgroundColor(activeTimerBackgroundColor);
        activeTimer.setTextColor(activeTimerTextColor);
        inactiveTimer.setBackgroundColor(disabledBackground);
        inactiveTimer.setTextColor(ChessUI.white);

        // 3. Define the Looper logic
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                boolean currentTurnIsWhite = game.IsWhiteTurn();
                long currentTime = game.GetTime(currentTurnIsWhite);

                if (currentTime > 0) {
                    int timerCountdownInterval = ChessLogic.Constants.timerCountdownInterval;
                    long newTime = currentTime - timerCountdownInterval;
                    game.SetTime(newTime, currentTurnIsWhite);

                    UpdateTimerText(isWhite == currentTurnIsWhite ? playerTimeTextView : opponentTimeTextView, newTime);

                    timerHandler.postDelayed(this, timerCountdownInterval);
                } else {
                    HandlePlayerOutOfTime();
                }
            }
        };

        // 4. Start the loop
        timerHandler.post(timerRunnable);
    }

    // --- Updates ---

    private void UpdateTimerText(TextView tv, long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        tv.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void UpdateCapturesUI() {
        String text1 = game.GetCapturesString(isWhite);
        String text2 = game.GetCapturesString(!isWhite);

        playerCapturedPiecesTextView.setText("Captured: " + text1);
        opponentCapturedPiecesTextView.setText("Captured: " + text2);
    }

    private void UpdateBoardUI() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int logicY = 7 - row;
                int logicX = col;

                if (!isWhite){
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

    private void UpdatePromotionMenuUI(ChessMove move) {
        promotionsMenu.removeAllViews();
        boolean isMovedPieceWhite = move.movedPiece.GetIsWhite();
        boolean isPromotionFar = isMovedPieceWhite == isWhite;
        boolean isPromotionOnFirstColumn = move.targetX == (isWhite ? 0 : 7);


        int visualCol = isWhite ? move.targetX : 7 - move.targetX;

        // 1. IMPORTANT: Add 8 invisible View objects to the first row
        // to "lock" the column widths to 1/8th of the board.
        for (int c = 0; c < 8; c++) {
            View spacer = new View(getContext());
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(0, 1f), // Row 0
                    GridLayout.spec(c, 1f)  // Columns 0-7
            );
            lp.width = 0;
            lp.height = 0;
            promotionsMenu.addView(spacer, lp);
        }

        // 2. IMPORTANT: Add 8 invisible View objects to the first column
        // to "lock" the row heights to 1/8th of the board.
        for (int r = 0; r < 8; r++) {
            View spacer = new View(getContext());
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(r , 1f), // Rows 0-7
                    GridLayout.spec(0, 1f)  // Column 0
            );
            lp.width = 0;
            lp.height = 0;
            promotionsMenu.addView(spacer, lp);
        }

        // 3. Now add your buttons. Because of the spacers above,
        // the grid is now forced into a perfect 8x8 ratio.
        ImageButton[] buttons = {promotionOptionQueen, promotionOptionKnight,
                promotionOptionRook, promotionOptionBishop, promotionOptionCancel};
        final int[] piecesTextures = {
                ChessLogic.Constants.whiteQueenIcon,
                ChessLogic.Constants.whiteKnightIcon,
                ChessLogic.Constants.whiteRookIcon,
                ChessLogic.Constants.whiteBishopIcon,
                ChessLogic.Constants.blackBishopIcon,
                ChessLogic.Constants.blackRookIcon,
                ChessLogic.Constants.blackKnightIcon,
                ChessLogic.Constants.blackQueenIcon
        };
        final int texturesLength = piecesTextures.length;

        for (int i = 0; i < buttons.length; i++) {
            ImageButton btn = buttons[i];
            int currentRow = isMovedPieceWhite ? i : (7 - i);

            int textureIndex = (isMovedPieceWhite) ? i : (texturesLength - i - 1);
            if (i < 4) btn.setImageResource(piecesTextures[textureIndex]);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(visualCol, 1f);
            params.rowSpec = GridLayout.spec(currentRow, 1f);

            btn.setLayoutParams(params);
            promotionsMenu.addView(btn);
        }
    }

    private void HandlePlayerOutOfTime() {
        System.out.println("TIME OUT for " + (game.IsWhiteTurn() ? "White" : "Black"));
    }

    // --- On Clicks ---

    private void OnPromotionClicked(int promotedPieceIndex, ChessMove move) {
        promotionsMenu.setVisibility(View.GONE);
        move.promotionPieceIndex = promotedPieceIndex;
        ExecuteMove(move);
    }

    private void OnSquareClicked(int x, int y) {
        if (!isSingleplayer && (isWhite != game.IsWhiteTurn())) {
            return;
        }

        promotionsMenu.setVisibility(View.GONE);
        ChessPiece clickedPiece = game.GetPieceAt(x, y);

        // 1. Attempting to move a selected piece (This works even if clickedPiece is null)
        ChessMove move = FindMoveInList(x, y);
        if (selectedPiece != null && move != null) {
            if (move.isPromotion) {
                UpdatePromotionMenuUI(move);
                promotionOptionQueen.setOnClickListener(v -> OnPromotionClicked(0, move));
                promotionOptionKnight.setOnClickListener(v -> OnPromotionClicked(1, move));
                promotionOptionRook.setOnClickListener(v -> OnPromotionClicked(2, move));
                promotionOptionBishop.setOnClickListener(v -> OnPromotionClicked(3, move));
                promotionsMenu.setVisibility(View.VISIBLE);
            } else {
                ExecuteMove(move);
            }
            return;
        }

        // 2. Selecting a piece - check for null first
        if (clickedPiece != null) {
            System.out.println("CLICKED ON: " + clickedPiece.toString());

            // Only allow selection if it's the current player's turn and color
            if (clickedPiece.GetIsWhite() == game.IsWhiteTurn()) {
                if (selectedPiece == clickedPiece) {
                    DeselectPiece();
                } else {
                    SelectPiece(clickedPiece);
                }
            } else {
                DeselectPiece();
            }
        } else {
            // Clicked an empty square that wasn't a valid move
            DeselectPiece();
        }

        UpdateBoardUI();
    }

}