package com.boardhub.chess.dataClasses;

public class User {
    private String UID;
    private String username;
    private String password;

    private long chessWins;
    private long chessLosses;
    private long chessDraws;

    public User(){}
    public User(String UID, String username, String password, int chessWins, int chessLosses, int chessDraws){
        this.UID = UID;
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
    public String getUsername() { return username; }
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
}
