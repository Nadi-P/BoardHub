package com.boardhub.chess.layouts;

import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.boardhub.BuildConfig;
import com.boardhub.R;
import com.boardhub.chess.dataClasses.ChessGame;
import com.boardhub.chess.dataClasses.ChessUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;

public class ChessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("BoardHubInit", "ChessActivity.onCreate START, BuildConfig.DEBUG=" + BuildConfig.DEBUG);
        try {
            FirebaseApp app = FirebaseApp.initializeApp(this);
            Log.i("BoardHubInit", "FirebaseApp initialized: " + (app != null ? app.getName() : "null"));

            FirebaseAppCheck appCheck = FirebaseAppCheck.getInstance();
            if (BuildConfig.DEBUG) {
                Log.i("BoardHubInit", "Installing DebugAppCheckProviderFactory");
                appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance());
            } else {
                Log.i("BoardHubInit", "Installing PlayIntegrityAppCheckProviderFactory");
                appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
            }
            Log.i("BoardHubInit", "AppCheck provider installed; requesting token...");
            appCheck.getAppCheckToken(false)
                    .addOnSuccessListener(token -> Log.i("BoardHubInit", "AppCheck token acquired (len=" + (token.getToken() != null ? token.getToken().length() : 0) + ")"))
                    .addOnFailureListener(e -> Log.e("BoardHubInit", "AppCheck token FAILED", e));
        } catch (Throwable t) {
            Log.e("BoardHubInit", "Firebase/AppCheck init threw", t);
        }

        ChessUI.SetChessFragmentManager(getSupportFragmentManager(), getBaseContext());
        setContentView(R.layout.chess_activity);
        ChessUI.ReplaceChessScreen(AuthLogin.newInstance());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                // Hide both the status bar (top) and navigation bar (bottom)
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());

                // Make it so they only appear briefly if the user swipes
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Fallback for older versions
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
//        ChessGame game = new ChessGame("UID", true, 2);
//        ChessUI.ReplaceChessScreen(ChessGameFragment.newInstance(game, true));
    }
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count <= 1) {
        } else {
            // If there are more than 1, allow the normal back behavior
            super.onBackPressed();
        }
    }
}