package com.boardhub.chess.dataClasses;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class User implements Serializable {
    private String UID;

    private String imageURL;
    private String username;
    private String password;

    private long chessWins;
    private long chessLosses;
    private long chessDraws;

    public User(){}
    public User(String UID, String imageURL, String username, String password, int chessWins, int chessLosses, int chessDraws){
        this.UID = UID;
        this.imageURL = imageURL;
        this.username = username;
        this.password = password;
        this.chessWins = chessWins;
        this.chessLosses = chessLosses;
        this.chessDraws = chessDraws;
    }
    public User(String UID, String username, String password){
        this.UID = UID;
        this.username = username;
        this.password = password;
        this.chessWins = 0;
        this.chessLosses = 0;
        this.chessDraws = 0;
    }

    public String getUid() { return UID; }
    public void setUid(String uid) { this.UID = uid; }
    public String getUsername() { return username; }
    public String getImageURL() { return imageURL; }
    public String getPassword() { return password; }
    public long getChessWins(){
        return this.chessWins;
    }
    public long getChessLosses(){
        return this.chessLosses;
    }
    public long getChessDraws(){
        return this.chessDraws;
    }

    public void AddChessWin(){
        this.chessWins++;
    }
    public void AddChessLoss(){
        this.chessLosses++;
    }
    public void AddChessDraw(){
        this.chessDraws++;
    }

    public void SetUsername(String username){
        this.username = username;
    }
    public void SetPassword(String password) {
        this.password = password;
    }
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", chessWins=" + chessWins +
                ", chessLosses=" + chessLosses +
                ", chessDraws=" + chessDraws;
    }

}
