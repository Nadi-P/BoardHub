package com.boardhub.chess.dataClasses;

// IMPORTANT: Use the androidx version, not android.app
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.boardhub.R;

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

    public static View CreateSChessGameOverPopup(boolean gameOverIndex, boolean gameOverReasonIndex, )
}