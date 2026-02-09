package com.boardhub.chess.layouts;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.boardhub.R;
import com.boardhub.chess.dataClasses.ChessDBI;
import com.boardhub.chess.dataClasses.ChessUI;

public class AuthSignup extends Fragment {

    public AuthSignup() {
        // Required empty public constructor
    }

    public static AuthSignup newInstance() {
        AuthSignup fragment = new AuthSignup();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.auth_signup, container, false);

        EditText etEmail = v.findViewById(R.id.etEmail);
        EditText etPassword = v.findViewById(R.id.etPassword);
        EditText etPhone = v.findViewById(R.id.etPhone);
        Button btnSignUp = v.findViewById(R.id.btnSignup);
        TextView tvLogin = v.findViewById(R.id.tvGoToSignUp);

        btnSignUp.setOnClickListener(view -> {
            ChessDBI.AttemptSignUp(etEmail, etPassword, (success, message) -> {
                if (success) {
                    Toast.makeText(getContext(), "Account Created!", Toast.LENGTH_SHORT).show();
                    ChessUI.ReplaceChessScreen(ChessStartGameMenu.newInstance(false));
                } else {
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvLogin.setOnClickListener(view -> {
            ChessUI.ReplaceChessScreen(new AuthLogin());
        });

        return v;
    }
}