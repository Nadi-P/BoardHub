package com.boardhub.chess.dataClasses;

// IMPORTANT: Use the androidx version, not android.app
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.boardhub.R;

import org.w3c.dom.Text;

public abstract class ChessUI {
    public static int white, black;
    public static int lightNormal, darkNormal;
    public static int lightHighlight, darkHighlight;
    public static int lightSelected, darkSelected;
    public static int disabledBackground;
    public static int subtextColor;
    public static int promotionMenuBackground;
    public static int promotionMenuPadding = 8;
    public static int queueingAnimationInterval = 300;

    private static FragmentManager chessFragmentManager;

    public static void SetChessFragmentManager(FragmentManager manager, Context context){
        ChessUI.chessFragmentManager = manager;

        white = ContextCompat.getColor(context, R.color.white);
        black = ContextCompat.getColor(context, R.color.black);

        lightNormal = ContextCompat.getColor(context, R.color.chess_square_light_normal);
        darkNormal = ContextCompat.getColor(context, R.color.chess_square_dark_normal);

        lightHighlight = ContextCompat.getColor(context, R.color.chess_square_light_highlight);
        darkHighlight = ContextCompat.getColor(context, R.color.chess_square_dark_highlight);

        lightSelected = ContextCompat.getColor(context, R.color.chess_square_light_selected);
        darkSelected = ContextCompat.getColor(context, R.color.chess_square_dark_selected);

        disabledBackground = ContextCompat.getColor(context, R.color.chess_timer_disabled_background);
        subtextColor = ContextCompat.getColor(context, R.color.chess_subtext_color);

        promotionMenuBackground = ContextCompat.getColor(context, R.color.chess_promotion_menu_background);
    }

    public static void ReplaceChessScreen(Fragment fragment){
        if (chessFragmentManager == null) return; // Prevent NullPointerException

        chessFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public static void ReturnToPreviousScreen(){
        if (chessFragmentManager == null) return; // Prevent NullPointerException
        chessFragmentManager.popBackStack();
    }    public static View CreateSChessGameOverPopup(
            LayoutInflater inflater, ViewGroup container,
            boolean isWin, int gameOverReasonIndex, int movesCount, boolean isWhiteWin){
        View popupWindow = inflater.inflate(R.layout.chess_gameover_popup, container, false);

        ImageView whiteCrown = popupWindow.findViewById(R.id.ivWhiteCrown);
        ImageView blackCrown = popupWindow.findViewById(R.id.ivBlackCrown);
        TextView tvWinStatus = popupWindow.findViewById(R.id.tvWinStatus);
        TextView tvWinReason = popupWindow.findViewById(R.id.tvWinReason);
        TextView tvMoveCount = popupWindow.findViewById(R.id.tvMoveCount);
        Button btnReviewGame = popupWindow.findViewById(R.id.btnReviewGame);
        Button btnMainMenu = popupWindow.findViewById(R.id.btnReturnToMenu);

        if (isWin) {
            if (isWhiteWin) {
                whiteCrown.setVisibility(View.VISIBLE);
                blackCrown.setVisibility(View.GONE);
                tvWinStatus.setText("WHITE WINS");
            }
            else {
                whiteCrown.setVisibility(View.GONE);
                blackCrown.setVisibility(View.VISIBLE);
                tvWinStatus.setText("BLACK WINS");
            }
            tvWinReason.setText(ChessLogic.Constants.winReasons[gameOverReasonIndex]);
        }
        else {
            whiteCrown.setVisibility(View.GONE);
            blackCrown.setVisibility(View.GONE);
            if (gameOverReasonIndex == 3) {
                tvWinStatus.setText("STALEMATE");
                tvWinReason.setText("");
            } else {
                tvWinStatus.setText("DRAW");
                tvWinReason.setText(ChessLogic.Constants.drawReasons[gameOverReasonIndex]);
            }
        }

        tvMoveCount.setText(String.valueOf(movesCount));

        btnReviewGame.setOnClickListener(v -> {
            popupWindow.setVisibility(View.GONE);
        });
        btnMainMenu.setOnClickListener(v -> {
            popupWindow.setVisibility(View.GONE);
            ReturnToPreviousScreen();
        });

        return popupWindow;
    }
}