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

public class AuthLogin extends Fragment {

    public AuthLogin() {
        // Required empty public constructor
    }

    public static AuthLogin newInstance() {
        AuthLogin fragment = new AuthLogin();
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.auth_login, container, false);

        EditText etEmail = v.findViewById(R.id.etEmail);
        EditText etPassword = v.findViewById(R.id.etPassword);
        Button btnLogin = v.findViewById(R.id.btnLogin);
        TextView tvSignUp = v.findViewById(R.id.tvGoToSignUp);

        btnLogin.setOnClickListener(view -> {
            ChessDBI.AttemptLogin(etEmail, etPassword, (success, message) -> {
                if (success) {
                    ChessUI.ReplaceChessScreen(ChessStartGameMenu.newInstance(false));
                } else {
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvSignUp.setOnClickListener(view -> {
            ChessUI.ReplaceChessScreen(new AuthSignup());
        });

        return v;
    }
}