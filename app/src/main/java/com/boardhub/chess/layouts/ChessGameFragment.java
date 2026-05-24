package com.boardhub.chess.layouts;

import static com.boardhub.chess.dataClasses.ChessUI.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import com.boardhub.R;
import com.boardhub.chess.dataClasses.*;
import com.boardhub.chess.pieces.ChessPiece;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.ListenerRegistration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

public class ChessGameFragment extends Fragment {
    private ListenerRegistration gameListener;
    private DatabaseReference statusRef;

    // timers
    private final android.os.Handler timerHandler = new android.os.Handler();
    private Runnable timerRunnable;

    // components
    private LayoutInflater inflater;
    private View screen;
    private ImageButton[][] boardSquares = new ImageButton[8][8];
    private TextView playerCapturedPiecesTextView,
            opponentCapturedPiecesTextView,
            playerTimeTextView,
            opponentTimeTextView,
            btnOptionsText,
            btnBackText,
            btnForwardText,
            drawOfferText,
            playerName,
            opponentName;

    private GridLayout promotionsMenu, chessGrid;
    private ImageButton promotionOptionQueen,
            promotionOptionKnight,
            promotionOptionRook,
            promotionOptionBishop,
            promotionOptionCancel;
    private ImageView
            btnOptionsIcon,
            btnBackIcon,
            btnForwardIcon,
            opponentAvatar,
            playerAvatar;
    private LinearLayout
            optionsSection,
            backSection,
            forwardSection,
            drawOfferPopup;
    private Button
            drawOfferAcceptButton,
            drawOfferDeclineButton,
            btnReturn;

    // logic
    private ChessGame game;
    private User player, opponent;
    private ChessPiece selectedPiece = null;
    private ArrayList<ChessMove> activeMoves = new ArrayList<>();
    private boolean isWhite;
    private int movePreviousIndex;
    private int moveInitialX = -1, moveInitialY = -1, moveTargetX = -1, moveTargetY = -1;
    private boolean isSingleplayer, isViewingPreviousMove, isGameOver;



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
        InitializeListeners();
        InitializeBoard();
        UpdateBoardUI();

        if (!isSingleplayer) {
            InitializeGameListener();
        }

        StartTimers();
        DisableBackButton();
        DisableForwardButton();
        return root;
    }

    // --- Initializations ---

    private void InitializeViews(View rootView){
        inflater = LayoutInflater.from(getContext());
        chessGrid = rootView.findViewById(R.id.chess_grid);
        promotionsMenu = rootView.findViewById(R.id.promotionsMenu_grid);

        playerTimeTextView = rootView.findViewById(R.id.playerTimeView);
        playerAvatar = rootView.findViewById(R.id.playerAvatar);
        playerName = rootView.findViewById(R.id.tvPlayerName);
        playerCapturedPiecesTextView = rootView.findViewById(R.id.playerCapturedPiecesTextView);

        opponentName = rootView.findViewById(R.id.tvOpponentName);
        opponentAvatar = rootView.findViewById(R.id.opponentAvatar);
        opponentTimeTextView = rootView.findViewById(R.id.opponentTimeView);
        opponentCapturedPiecesTextView = rootView.findViewById(R.id.opponentCapturedPiecesTextView);

        optionsSection = rootView.findViewById(R.id.optionsSection);
        btnOptionsIcon = rootView.findViewById(R.id.btnOptionsIcon);
        btnOptionsText = rootView.findViewById(R.id.btnOptionsText);

        backSection = rootView.findViewById(R.id.backSection);
        btnBackIcon = rootView.findViewById(R.id.btnBackIcon);
        btnBackText = rootView.findViewById(R.id.btnBackText);

        forwardSection = rootView.findViewById(R.id.forwardSection);
        btnForwardIcon = rootView.findViewById(R.id.btnForwardIcon);
        btnForwardText = rootView.findViewById(R.id.btnForwardText);

        drawOfferPopup = rootView.findViewById(R.id.drawOfferPopup);
        drawOfferText = rootView.findViewById(R.id.drawOfferText);
        drawOfferAcceptButton = rootView.findViewById(R.id.btnAcceptDrawOffer);
        drawOfferDeclineButton = rootView.findViewById(R.id.btnDeclineDrawOffer);

        btnReturn = rootView.findViewById(R.id.btnReturn);

        screen = rootView;
    }
    private void InitializeBoard() {
        chessGrid.removeAllViews();

        if (isSingleplayer) {
            playerName.setText("White Player");
            opponentName.setText("Black Player");
        }
        else {
            // 1. Fetch the White player (true)
            ChessDBI.getUserFromUID(game.GetPlayerUID(true), player -> {
                this.player = player; // Result of getUserFromUID includes the URI now

                // 2. Once White is ready, fetch the Black player (false)
                ChessDBI.getUserFromUID(game.GetPlayerUID(false), opponent -> {
                    this.opponent = opponent;

                    // 3. NOW both User objects (with their URIs) are ready
                    UpdateScoreboardNames();
                });
            });
        }

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
            ChessMove receivedMove = new ChessMove(game, packet);
            if (HandleIrregularMove(receivedMove)) {
                System.out.println("Detected Irregular move");
                return;
            }

            if (isWhiteTurnInPacket == game.IsWhiteTurn()) {
                receivedMove.Execute();

                moveInitialX = receivedMove.initialX;
                moveInitialY = receivedMove.initialY;
                moveTargetX = receivedMove.targetX;
                moveTargetY = receivedMove.targetY;

                game.SetTime((long) packet.get("whiteTime"), true);
                game.SetTime((long) packet.get("blackTime"), false);

                UpdateDisplayAfterMove();

                isViewingPreviousMove = false;
                movePreviousIndex = game.GetMovesRecord().size() - 1;
                DisableForwardButton();
                if (moveInitialX != -1) {
                    EnableBackButton();
                }

                if (receivedMove.isCheckmate) HandleCheckmate(receivedMove.isWhiteTurn);
                else if (receivedMove.isStalemate) HandleStalemate(receivedMove.isWhiteTurn);
                else if (receivedMove.isDraw) HandleDraw(receivedMove.isWhiteTurn);
            }
        });
    }
    private void InitializeListeners(){
        drawOfferAcceptButton.setOnClickListener(v -> {
            ChessMove move = new ChessMove(game, 6);
            ExecuteMove(move);
        });
        drawOfferDeclineButton.setOnClickListener(v -> {
            ChessMove move = new ChessMove(game, 7);
            ExecuteMove(move);
        });
        optionsSection.setOnClickListener(v -> {
            ChessUI.AnimateButtonClickShrink(v, getContext());
            ShowOptionsDialog();
        });
        btnReturn.setOnClickListener(v -> {
            ChessUI.ReturnToPreviousScreen();
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ShowOptionsDialog();
            }
        });
    }

    // --- Operations ---
    private void ShowOptionsDialog(){
        String[] colors = {"Draw", "Resign"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.BigDarkDialog);
        builder.setItems(colors, (dialog, which) -> {
            // 'which' is the index of the clicked item
            String selectedOption = colors[which];
            HandleOptionsDialog(selectedOption);
        });
        builder.show();
    }
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
        move.CheckIsCheckmate();
        System.out.println("Executing move");

        if (!isSingleplayer){
            ChessDBI.SaveMove(move);
        }
        else {
            move.Execute();

            moveInitialX = move.initialX;
            moveInitialY = move.initialY;
            moveTargetX = move.targetX;
            moveTargetY = move.targetY;

            UpdateDisplayAfterMove();

            isViewingPreviousMove = false;
            movePreviousIndex = game.GetMovesRecord().size()-1;
            DisableForwardButton();
            EnableBackButton();

            if (HandleIrregularMove(move)) return;
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
    private void HandleOptionsDialog(String selection) {
        if (selection == "Draw") {
            // Handle draw
            ChessMove drawMove = new ChessMove(game, 5);
            drawMove.isWhiteTurn = isWhite;
            ExecuteMove(drawMove);
        } else if (selection == "Resign") {
            ChessMove resignMove = new ChessMove(game, 3);
            resignMove.isWhiteTurn = isWhite;
            ExecuteMove(resignMove);
        }
        else {
            return;
        }
    }

    // --- Timers ---

    private void StartTimers(){
        SwitchTimers();
        SwitchTimers();
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
                    ChessMove move = new ChessMove(game, 4);
                    ExecuteMove(move);
                }
            }
        };

        // 4. Start the loop
        timerHandler.post(timerRunnable);
    }
    private void StopTimers() {
        timerHandler.
                removeCallbacks(timerRunnable);
    }

    // --- Updates ---

    private void UpdateDisplayAfterMove() {
        movePreviousIndex = game.GetMovesRecord().size()-1;
        UpdateCapturesDisplay();
        DeselectPiece();
        SwitchTimers();
        UpdateBoardUI();
    }
    private void UpdateCapturesDisplay() {
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
                if (
                    IsMoveTarget(logicX, logicY) ||
                    (moveInitialX == logicX && moveInitialY == logicY) ||
                    (moveTargetX == logicX && moveTargetY == logicY)) {
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
    private void UpdatePromotionMenuPosition(ChessMove move) {
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
    private void UpdateTimerText(TextView tv, long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        tv.setText(String.format("%02d:%02d", minutes, seconds));
    }
    private void UpdateScoreboardNames() {
        if (player != null && opponent != null) {
            playerName.setText(player.getUsername());
            opponentName.setText(opponent.getUsername());
            ChessDBI.LoadImageToView(getActivity(), playerAvatar, player.getImageURL());
            ChessDBI.LoadImageToView(getActivity(), opponentAvatar, opponent.getImageURL());
        }
    }

    // --- Game Ends ---

    private boolean HandleIrregularMove(ChessMove move) {
        if (move.isResignation) {
            HandleResignation(move.isWhiteTurn);
            return true;
        } else if (move.isOutOfTime) {
            HandleOutOfTime(move.isWhiteTurn);
            return true;
        } else if (move.isDrawOffer) {
            HandleDrawOffer(move);
            return true;
        } else if (move.isDrawAccept) {
            HandleAgreedDraw(move.isWhiteTurn);
            return true;
        } else if (move.isDrawDecline) {
            HandleDrawOfferDecline(move);
            return true;
        } else if (move.isRepetition) {
            HandleRepetition(move.isWhiteTurn);
            return true;
        }
        return false;
    }
    private void HandleDrawOffer(ChessMove move) {
        if (move.isWhiteTurn == isWhite) {
            drawOfferText.setTextColor(white);
            drawOfferText.setText("Draw Pending...");
            drawOfferAcceptButton.setVisibility(View.GONE);
            drawOfferDeclineButton.setVisibility(View.GONE);
        } else {
            drawOfferText.setTextColor(white);
            drawOfferText.setText("Accept Draw?");
            drawOfferAcceptButton.setVisibility(View.VISIBLE);
            drawOfferDeclineButton.setVisibility(View.VISIBLE);
        }
        drawOfferPopup.setVisibility(View.VISIBLE);
    }
    private void HandleDrawOfferDecline(ChessMove move) {
        boolean isWhiteTurn = move.isWhiteTurn;
        if (isWhiteTurn == isWhite) {
            drawOfferText.setTextColor(subtextColor);
            drawOfferText.setText("Draw Declined");
        }
        drawOfferPopup.setVisibility(View.GONE);
    }
    private void HandleDraw(boolean isMoveByWhite) {
        HandleEndGame(isMoveByWhite, false, 0);
    }
    private void HandleAgreedDraw(boolean isMoveByWhite) {
        HandleEndGame(isMoveByWhite, false, 1);
        drawOfferPopup.setVisibility(View.GONE);

    }
    private void HandleRepetition(boolean isMoveByWhite) {
        HandleEndGame(isMoveByWhite, false, 2);
    }
    private void HandleStalemate(boolean isMoveByWhite) {
        HandleEndGame(isMoveByWhite, false, 3);
    }
    private void HandleCheckmate(boolean isMoveByWhite) {
        // The mover delivered checkmate, so they win — pass the loser to keep HandleEndGame's !isMoveByWhite logic correct.
        HandleEndGame(!isMoveByWhite, true, 0);
    }
    private void HandleResignation(boolean isMoveByWhite) {
        HandleEndGame(isMoveByWhite, true, 2);
    }
    private void HandleOutOfTime(boolean isMoveByWhite) {
        HandleEndGame(isMoveByWhite, true, 1);
    }
    private void HandleEndGame(boolean isMoveByWhite, boolean isWIn, int gameOverReasonIndex) {
        StopTimers();
        if (!isGameOver) {
            if (isWIn) {
                boolean localPlayerWon = (isWhite != isMoveByWhite);
                ChessDBI.RecordGameResult(localPlayerWon ? "chessWins" : "chessLosses");
            } else {
                ChessDBI.RecordGameResult("chessDraws");
            }
            if (!isSingleplayer && game != null) {
                ChessDBI.DeleteGame(game.GetUID());
            }
        }
        isGameOver = true;
        if (gameListener != null) {
            gameListener.remove();
            gameListener = null;
        }
        activeMoves.clear();
        DisableOptionsButton();

        String whiteAvatarUrl = isWhite ? player.getImageURL() : opponent.getImageURL();
        String blackAvatarUrl = isWhite ? opponent.getImageURL() : player.getImageURL();

        View popup = ChessUI.CreateSChessGameOverPopup(
                inflater,
                (ViewGroup) screen,
                isWIn,
                gameOverReasonIndex,
                game.GetMovesRecord().size(),
                !isMoveByWhite,
                getActivity(),
                whiteAvatarUrl,
                blackAvatarUrl
        );

        Button btnReviewGame = popup.findViewById(R.id.btnReviewGame);
        Button btnBackToMenu = popup.findViewById(R.id.btnReturnToMenu);

        btnReviewGame.setOnClickListener(v -> {
            ChessUI.AnimateButtonClickShrink(v, getContext());
            popup.setVisibility(View.GONE);
            btnReturn.setVisibility(View.VISIBLE);
        });
        btnBackToMenu.setOnClickListener(v -> {
            ChessUI.AnimateButtonClickShrink(v, getContext());
            ChessUI.ReturnToPreviousScreen();
        });

        ((ViewGroup) screen).addView(popup);
    }

    // --- On Clicks ---

    private void OnPromotionClicked(int promotedPieceIndex, ChessMove move) {
        promotionsMenu.setVisibility(View.GONE);
        move.promotionPieceIndex = promotedPieceIndex;
        ExecuteMove(move);
    }
    private void OnSquareClicked(int x, int y) {
        if (isGameOver || isViewingPreviousMove) {
            return;
        }
        if (!isSingleplayer && (isWhite != game.IsWhiteTurn())) {
            return;
        }

        System.out.println("Passed");

        promotionsMenu.setVisibility(View.GONE);
        ChessPiece clickedPiece = game.GetPieceAt(x, y);

        // 1. Attempting to move a selected piece (This works even if clickedPiece is null)
        ChessMove move = FindMoveInList(x, y);
        if (selectedPiece != null && move != null) {
            if (move.isPromotion) {
                UpdatePromotionMenuPosition(move);
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

    // --- Enables ---
    private void DisableOptionsButton() {
        optionsSection.setOnClickListener(v -> {});
        btnOptionsIcon.setImageTintList(ColorStateList.valueOf(ChessUI.disabledBackground));
        btnOptionsText.setTextColor(ColorStateList.valueOf(ChessUI.disabledBackground));
        optionsSection.setEnabled(false);
    }
    private void DisableBackButton() {
        backSection.setOnClickListener(v -> {});
        btnBackIcon.setImageTintList(ColorStateList.valueOf(disabledBackground));
        btnBackText.setTextColor(ColorStateList.valueOf(ChessUI.disabledBackground));
    }
    private void DisableForwardButton() {
        forwardSection.setOnClickListener(v -> {});
        btnForwardIcon.setImageTintList(ColorStateList.valueOf(ChessUI.disabledBackground));
        btnForwardText.setTextColor(ColorStateList.valueOf(ChessUI.disabledBackground));
    }
    private void EnableBackButton() {
        ChessUI.AnimateButtonClickShrink(backSection, getContext());

        btnBackIcon.setImageTintList(ColorStateList.valueOf(ChessUI.subtextColor));
        btnBackText.setTextColor(ColorStateList.valueOf(ChessUI.subtextColor));
        backSection.setOnClickListener(v -> {
            if (movePreviousIndex >= 0) {
                EnableForwardButton();
                isViewingPreviousMove = true;
                movePreviousIndex--;

                // Load the move at the current index (it now contains the AFTER state)
                ChessMove moveToShow = (movePreviousIndex >= 0) ? game.GetMovesRecord().get(movePreviousIndex) : null;
                LoadPreviousMove(moveToShow);

                if (movePreviousIndex <= -1) {
                    DisableBackButton();
                }
            }
        });
    }
    private void EnableForwardButton() {
        ChessUI.AnimateButtonClickShrink(forwardSection, getContext());

        // FIX: Set colors for FORWARD icons, not back icons
        btnForwardIcon.setImageTintList(ColorStateList.valueOf(ChessUI.subtextColor));
        btnForwardText.setTextColor(ColorStateList.valueOf(ChessUI.subtextColor));

        // FIX: Set listener on FORWARD section
        forwardSection.setOnClickListener(v -> {
            EnableBackButton();

            movePreviousIndex++;

            // If the index exceeds the last move made, return to the live game
            if (movePreviousIndex >= game.GetMovesRecord().size()-1) {
                DisableForwardButton();
                isViewingPreviousMove = false;
                UpdateBoardUI(); // Return to the actual current board state
            }
            else {
                ChessMove move = game.GetMovesRecord().get(movePreviousIndex);
                LoadPreviousMove(move);
            }
        });
    }
    private void LoadPreviousMove(ChessMove move) {
        // FEN is now a flat 64-char string (Top-Down)
        if (move != null) {
            String fen = move.boardFen;
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {

                    int logicX = isWhite ? col : 7 - col;
                    int logicY = isWhite ? 7 - row : row;

                    int fenRow = 7 - logicY;
                    int fenCol = logicX;
                    int fenIndex = fenRow * 8 + fenCol;
                    String pieceChar = String.valueOf(fen.charAt(fenIndex));

                    // 3. Check if this square was the start or end of the move
                    boolean isMatch = (logicX == move.initialX && logicY == move.initialY) ||
                            (logicX == move.targetX && logicY == move.targetY);

                    // 4. Update the Square
                    ImageButton square = boardSquares[row][col];
                    int pieceIcon = ChessLogic.Constants.FENtoIconMap.get(pieceChar);
                    square.setImageResource(pieceIcon);

                    boolean isLight = (row + col) % 2 == 0;
                    square.setBackgroundColor(isMatch ?
                            (isLight ? lightHighlight : darkHighlight) :
                            (isLight ? lightNormal : darkNormal));
                }
            }
        } else {
            String fen = game.GetInitialFEN();
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    // 1. Get Logical Coordinates based on perspective
                    int logicX = isWhite ? col : 7 - col;
                    int logicY = isWhite ? 7 - row : row;

                    // 2. Map Logical Coordinates to FEN Index (Top-Down: Rank 8 to 1)
                    int fenRow = 7 - logicY;
                    int fenCol = logicX;
                    int fenIndex = fenRow * 8 + fenCol;

                    String pieceChar = String.valueOf(fen.charAt(fenIndex));

                    // 3. Update UI
                    ImageButton square = boardSquares[row][col];

                    // Safe lookup to avoid NullPointerException
                    Integer icon = ChessLogic.Constants.FENtoIconMap.get(pieceChar);
                    int pieceIcon = (icon != null) ? icon : 0;

                    if (pieceIcon != 0) {
                        square.setImageResource(pieceIcon);
                    } else {
                        square.setImageDrawable(null);
                    }

                    // 4. Reset colors to normal (no highlights for the starting position)
                    boolean isLight = (row + col) % 2 == 0;
                    square.setBackgroundColor(isLight ? lightNormal : darkNormal);
                }
            }
        }
    }
}