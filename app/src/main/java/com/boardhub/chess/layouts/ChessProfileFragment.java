package com.boardhub.chess.layouts;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boardhub.R;

public class ChessProfileFragment extends Fragment {


    public ChessProfileFragment() {
        // Required empty public constructor
    }

    public static ChessProfileFragment newInstance() {
        ChessProfileFragment fragment = new ChessProfileFragment();
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
        return inflater.inflate(R.layout.chess_profile_fragment, container, false);
    }
}