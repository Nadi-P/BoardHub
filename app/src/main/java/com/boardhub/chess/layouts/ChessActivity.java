package com.boardhub.chess.layouts;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.boardhub.R;
import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessUI;
import com.google.firebase.auth.FirebaseAuth;

public class ChessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChessUI.SetChessFragmentManager(getSupportFragmentManager(), getBaseContext());

        setContentView(R.layout.chess_activity);
        ChessUI.ReplaceChessScreen(AuthLogin.newInstance());
//        ChessGame game = new ChessGame("UID", true, 0);
//        ChessUI.ReplaceChessScreen(ChessGameFragment.newInstance(game, true));
    }
}