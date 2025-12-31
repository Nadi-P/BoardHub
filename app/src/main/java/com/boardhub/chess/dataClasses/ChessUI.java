package com.boardhub.chess.dataClasses;

// IMPORTANT: Use the androidx version, not android.app
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.boardhub.R;

public class ChessUI {
    private static FragmentManager chessFragmentManager;

    public static void SetChessFragmentManager(FragmentManager manager){
        ChessUI.chessFragmentManager = manager;
    }

    public static void ReplaceChessScreen(Fragment fragment){
        if (chessFragmentManager == null) return; // Prevent NullPointerException

        chessFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}