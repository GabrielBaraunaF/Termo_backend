package com.termoduelo.servidor.jogo;

import com.termoduelo.model.Player;

import java.util.HashMap;
import java.util.Map;

public class Game {

    private static final int MAX_ATTEMPTS = 6;

    private final Map<Integer, Integer> attempts = new HashMap<>();

    private boolean finished;

    private int gameID;

    private String word;

    private Player playerOne;

    private Player playerTwo;

    private static int numberGame=1;

    public Game(int gameID, String word, Player playerOne, Player playerTwo) {
        this.gameID = gameID;
        this.word = word;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        attempts.put(playerOne.getId(), 0);
        attempts.put(playerTwo.getId(), 0);
    }

    public int registerAttempt(int userId) {
        return attempts.merge(userId, 1, Integer::sum);
    }

    public boolean hasAttemptsLeft(int userId) {
        return attempts.get(userId) < MAX_ATTEMPTS;
    }

    public  int getAttemptsUser(int userId) {
        return attempts.get(userId);
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public void setPlayerOne(Player playerOne) {
        this.playerOne = playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public void setPlayerTwo(Player playerTwo) {
        this.playerTwo = playerTwo;
    }

    public static int getNumberGame() {
        return numberGame;
    }

    public static void setNumberGame(int numberGame) {
        Game.numberGame = numberGame;
    }

    public Player getPlayer(int userId) {
        if (playerOne.getId() == userId) return playerOne;
        if (playerTwo.getId() == userId) return playerTwo;
        throw new IllegalArgumentException("Jogador não pertence a esta partida");
    }

    public Player getOpponent(int userId) {
        return getPlayer(userId) == playerOne ? playerTwo : playerOne;
    }
}
