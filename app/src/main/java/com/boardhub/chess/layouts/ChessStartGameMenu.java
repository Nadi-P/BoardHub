package com.boardhub.chess.layouts;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.boardhub.R;
import com.boardhub.chess.dataClasses.ChessDBI;
import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessUI;
import com.boardhub.chess.dataClasses.User;
import com.google.firebase.auth.FirebaseAuth;

public class ChessStartGameMenu extends Fragment {
    public static final String IS_SINGLEPLAYER = "isSinglePlayer";
    private boolean isSingleplayer;

    private User user;

    private final android.os.Handler timerHandler = new android.os.Handler();
    private Runnable timerRunnable;
    private int intervalsMade;

    private int currentModeIndex = 2;
    private final String[] modes = {"\uD83E\uDDE8 Bullet (1m)", "⚡ Blitz (3m)", "⌛ Rapid (10m)", "⏱\uFE0F Classic (30m)"};
    private final int[] durations = {1, 3, 10, 30}; // in minutes
    private boolean isStartGameBtnClicked;

    private String selectedColor = "RANDOM";

    private Button btnSelectMode, btnStartGame;
    ImageButton btnLogout, btnProfile;
    private FrameLayout frameBlack, frameRandom, frameWhite;
    private TextView tvQueueing;

    public ChessStartGameMenu() {
        // Required empty public constructor
    }

    public static ChessStartGameMenu newInstance(boolean isSingleplayer) {
        ChessStartGameMenu fragment = new ChessStartGameMenu();
        Bundle args = new Bundle();
        args.putSerializable(IS_SINGLEPLAYER, isSingleplayer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isSingleplayer = (boolean) getArguments().getSerializable(IS_SINGLEPLAYER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.chess_start_game_menu_fragment, container, false);
        GetUser(root);
        return root;
    }
    private void Main(View root){
        InitializeViews(root);
        setupListeners(root);
    }
    private void InitializeViews(View root) {
        // Initialize Views
        btnSelectMode = root.findViewById(R.id.btn_select_mode); // Add this ID to your XML
        btnStartGame = root.findViewById(R.id.btn_start_game);
        frameBlack = root.findViewById(R.id.select_side_black); // Add IDs to outer FrameLayouts
        frameRandom = root.findViewById(R.id.select_side_random);
        frameWhite = root.findViewById(R.id.select_side_white);
        tvQueueing = root.findViewById(R.id.queueing_tv);
        btnLogout = root.findViewById(R.id.btnLogout);
        btnProfile = root.findViewById(R.id.btnProfile);

        btnSelectMode.setText(modes[currentModeIndex]);
        tvQueueing.setVisibility(View.INVISIBLE);

        isStartGameBtnClicked = !btnStartGame.getText().equals("Start Game");

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                intervalsMade += 1;
                timerHandler.postDelayed(this, ChessUI.queueingAnimationInterval);
                if (intervalsMade < 4){
                    tvQueueing.setText(tvQueueing.getText() + ".");
                }
                else if (intervalsMade > 6) {
                    intervalsMade = 0;
                    tvQueueing.setText("Queueing");
                }
            }
        };
    }
    private void setupListeners(View root) {
        btnStartGame.setOnClickListener(v -> {
            ChessUI.AnimateButtonClickShrink(v, getContext());

            int timeInMillis = durations[currentModeIndex] * 60 * 1000;
            if (isSingleplayer) {
                ChessGame game = new ChessGame("UID", true, "", "", timeInMillis);
                ChessGameFragment fragment = ChessGameFragment.newInstance(game, isSingleplayer);
                ChessUI.ReplaceChessScreen(fragment);
            }
            else {
                if (!isStartGameBtnClicked){
                    isStartGameBtnClicked = true;
                    startQueueing();
                }
                else {
                    isStartGameBtnClicked = false;
                    endQueueing();
                }
            }
        });

        btnSelectMode.setOnClickListener(v -> {
            ChessUI.AnimateButtonClickShrink(v, getContext());

            currentModeIndex = (currentModeIndex - 1 + modes.length) % modes.length;
            btnSelectMode.setText(modes[currentModeIndex]);
        });

        btnLogout.setOnClickListener(v -> {
            ChessUI.ReturnToPreviousScreen();
        });

        btnProfile.setOnClickListener(v -> {
            ChessUI.ReplaceChessScreen(ChessProfileFragment.newInstance(user));
        });

        setupColorSelectors();
    }
    private void setupColorSelectors() {
        View.OnClickListener colorListener = v -> {
            ChessUI.AnimateButtonClickShrink(v, getContext());

            // Reset all backgrounds to null
            frameBlack.setBackground(null);
            frameRandom.setBackground(null);
            frameWhite.setBackground(null);

            // Set selected background and update state
            v.setBackgroundResource(R.drawable.chess_green_button_background);

            if (v == frameBlack) selectedColor = "BLACK";
            else if (v == frameWhite) selectedColor = "WHITE";
            else selectedColor = "RANDOM";
        };

        frameBlack.setOnClickListener(colorListener);
        frameRandom.setOnClickListener(colorListener);
        frameWhite.setOnClickListener(colorListener);

        // Set default selection
        frameRandom.performClick();
    }
    private void startQueueing(){
        btnStartGame.setBackgroundResource(R.drawable.chess_orange_button_background);
        btnStartGame.setText("Cancel");
        tvQueueing.setText("Queueing");
        tvQueueing.setVisibility(View.VISIBLE);

        timerHandler.post(timerRunnable);

        ChessDBI.AddPlayerToGameQueue(currentModeIndex, selectedColor, (gameUID, isWhite, userUID, opponentUID) -> {
            endQueueing();
            ChessGame game = new ChessGame(gameUID, isWhite, userUID, opponentUID, currentModeIndex);
            ChessUI.ReplaceChessScreen(ChessGameFragment.newInstance(game, false));
        });
    }
    private void endQueueing(){
        btnStartGame.setBackgroundResource(R.drawable.chess_green_button_background);
        btnStartGame.setText("Start Game");
        tvQueueing.setText("");
        tvQueueing.setVisibility(View.INVISIBLE);

        timerHandler.removeCallbacks(timerRunnable);
        intervalsMade = 0;

        ChessDBI.RemovePlayerFromGameQueue();
    }
    private void GetUser(View root) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ChessDBI.getUserFromUID(uid, new ChessDBI.UserCallback() {
            @Override
            public void onCallback(User user) {
                if (user != null) {
                    ChessStartGameMenu.this.user = user;
                    Main(root);
                } else {
                    Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}