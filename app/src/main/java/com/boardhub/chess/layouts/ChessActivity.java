package com.boardhub.chess.layouts;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.boardhub.R;
import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessUI;

public class ChessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChessUI.SetChessFragmentManager(getSupportFragmentManager(), getBaseContext());
        boolean showGameScreen = false;
        boolean showWhiteSide = true;
        if (showGameScreen) {
            ChessGame game = new ChessGame("a", showWhiteSide, 10*60*1000);
            ChessUI.ReplaceChessScreen(ChessGameFragment.newInstance(game, true));
        }
        else {
            ChessUI.ReplaceChessScreen(ChessStartGameMenu.newInstance(false));
        }
        setContentView(R.layout.chess_activity);
    }
}