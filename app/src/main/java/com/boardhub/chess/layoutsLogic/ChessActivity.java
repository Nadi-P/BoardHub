package com.boardhub.chess.layoutsLogic;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.boardhub.R;
import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessPlayer;
import com.boardhub.chess.dataClasses.ChessUI;

public class ChessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChessUI.SetChessFragmentManager(getSupportFragmentManager(), getBaseContext());
        ChessGame board = new ChessGame(2, 0);
        ChessPlayer player = (false) ? board.GetWhitePlayer() : board.GetBlackPlayer();
        ChessUI.ReplaceChessScreen(ChessGameFragment.newInstance(player));
        setContentView(R.layout.chess_activity);
    }
}