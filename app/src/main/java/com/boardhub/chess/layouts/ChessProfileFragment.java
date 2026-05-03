package com.boardhub.chess.layouts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.boardhub.R;
import com.boardhub.chess.dataClasses.ChessDBI;
import com.boardhub.chess.dataClasses.ChessLogic;
import com.boardhub.chess.dataClasses.ChessMove;
import com.boardhub.chess.dataClasses.ChessUI;
import com.boardhub.chess.dataClasses.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class ChessProfileFragment extends Fragment {

    private ImageButton imgProfile;
    private TextView tvWins, tvLosses, tvDraws, tvRatio;
    private EditText editUsername;
    private Uri selectedImageUri;
    private Button btnReturn, btnSave;
    private User user;

    // 1. Initialize the Photo Picker launcher
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgProfile.setImageURI(uri); // Show preview locally
                }
            });

    public ChessProfileFragment() {
        // Required empty public constructor
    }

    public static ChessProfileFragment newInstance(User user) {
        ChessProfileFragment fragment = new ChessProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chess_profile_fragment, container, false);
        InitializeViews(view);
        SetupListeners();
        updateUI(user);
        // Save button logic
        return view;
    }
    private void InitializeViews(View root){
        imgProfile = root.findViewById(R.id.imgProfile);
        tvWins = root.findViewById(R.id.tvWins);
        tvLosses = root.findViewById(R.id.tvLosses);
        tvDraws = root.findViewById(R.id.tvDraws);
        editUsername = root.findViewById(R.id.editUsername);
        btnReturn = root.findViewById(R.id.btnReturn);
        btnSave = root.findViewById(R.id.btnSave);
    }
    private void SetupListeners(){
        btnSave.setOnClickListener(v -> {
            boolean check = false;
            if (selectedImageUri != null) {
                uploadImageToFirebase();
                check = true;
            }
            if (!editUsername.getText().toString().equals(user.getUsername())) {
                user.SetUsername(editUsername.getText().toString());
                check = true;
            }
            if (check){
                ChessDBI.UpdateUser(user);
                Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                ChessUI.ReturnToPreviousScreen();
            }
            else {
                Toast.makeText(getContext(), "No Changes Were Made", Toast.LENGTH_SHORT).show();
            }
        });

        btnReturn.setOnClickListener(v -> {
            ChessUI.ReturnToPreviousScreen();
        });


        imgProfile.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));
    }
    private void updateUI(User user) {
        editUsername.setText(user.getUsername());
        tvWins.setText(String.valueOf(user.getChessWins()));
        tvLosses.setText(String.valueOf(user.getChessLosses()));
        tvDraws.setText(String.valueOf(user.getChessDraws()));
        ChessDBI.LoadImageToView(getActivity(), imgProfile, user.getImageURL());
    }
    private void uploadImageToFirebase() {
        if (selectedImageUri == null) return;

        Context ctx = getContext();
        if (ctx == null) return;
        Context appCtx = ctx.getApplicationContext();

        String uid = user.getUid();

        try {
            InputStream inputStream = ctx.getContentResolver().openInputStream(selectedImageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            Bitmap squareBitmap = ChessLogic.cropToSquare(originalBitmap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            squareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] data = baos.toByteArray();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("profile_pics/" + uid + ".jpg");

            storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        user.setImageURL(downloadUrl);
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update("photoUrl", downloadUrl);

                        if (isAdded() && imgProfile != null) {
                            imgProfile.setImageBitmap(squareBitmap);
                        }

                        Toast.makeText(appCtx, "Photo Uploaded & Cropped!", Toast.LENGTH_SHORT).show();

                        if (originalBitmap != null) originalBitmap.recycle();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(appCtx, "Upload Failed", Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(appCtx, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }
}