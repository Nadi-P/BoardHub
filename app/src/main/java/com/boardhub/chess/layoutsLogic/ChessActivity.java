package com.boardhub.chess.layoutsLogic;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.boardhub.R;
import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessUI;

public class ChessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChessUI.SetChessFragmentManager(getSupportFragmentManager());
        ChessGame board = new ChessGame(2, 0);
        ChessUI.ReplaceChessScreen(ChessGameFragment.newInstance(board.GetWhitePlayer()));
        setContentView(R.layout.chess_activity);
    }
}