package com.boardhub.chess.dataClasses;

// IMPORTANT: Use the androidx version, not android.app
import android.content.Context;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.boardhub.R;

public class ChessUI {
    public static int lightNormal, darkNormal;
    public static int lightHighlight, darkHighlight;
    public static int lightSelected, darkSelected;
    private static FragmentManager chessFragmentManager;

    public static void SetChessFragmentManager(FragmentManager manager, Context context){
        ChessUI.chessFragmentManager = manager;

        lightNormal = ContextCompat.getColor(context, R.color.chess_square_light_normal);
        darkNormal = ContextCompat.getColor(context, R.color.chess_square_dark_normal);

        lightHighlight = ContextCompat.getColor(context, R.color.chess_square_light_highlight);
        darkHighlight = ContextCompat.getColor(context, R.color.chess_square_dark_highlight);

        lightSelected = ContextCompat.getColor(context, R.color.chess_square_light_selected);
        darkSelected = ContextCompat.getColor(context, R.color.chess_square_dark_selected);
    }

    public static void ReplaceChessScreen(Fragment fragment){
        if (chessFragmentManager == null) return; // Prevent NullPointerException

        chessFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}