package com.termoduelo.servidor.jogo;

import com.termoduelo.model.Player;
import com.termoduelo.model.Status;
import com.termoduelo.proto.*;

import java.util.*;

public class GameService {

    PlayerQueue playerQueue = new PlayerQueue();

    Map<Integer, Game> matches = new HashMap<>();

    Random random = new Random();

    List<String> words = List.of(
            "CASAS",
            "LIVRO",
            "MUNDO",
            "PEDRA",
            "FESTA",
            "CARRO",
            "PLANO",
            "NOITE",
            "VERDE",
            "PRAIA"
    );


    public void startMatch() {

        System.out.println(playerQueue.size());
        Player playerOne = playerQueue.dequeue();
        Player playerTwo = playerQueue.dequeue();
        String word = words.get(random.nextInt(10));

        System.out.println(playerOne.getName());
        System.out.println(playerTwo.getName());

        Game newGame = new Game(1, word, playerOne, playerTwo);

        notifyGameStarted(playerOne, playerTwo, newGame);
        notifyGameStarted(playerTwo, playerOne, newGame);

        playerOne.setStatus(Status.JOGANDO);
        playerTwo.setStatus(Status.JOGANDO);

        matches.put(newGame.getGameID(), newGame);

    }

    public GuessResult submitGuess(EnviarPalpiteRequest request) {

        Game game = matches.get(request.getGameId());
        Player player = game.getPlayer(request.getUserId());
        int userId = player.getId();

        String word = game.getWord();
        String attempt = request.getPalpite();
        HashMap<Character, Integer> letterCounts = new HashMap<>();
        int correctLetters = 0;

        char[] wordChar = word.toCharArray();
        char[] attemptChars = attempt.toCharArray();
        String[] result = new String[word.length()];
        Arrays.fill(result, "errado");

        for (char letter : wordChar) {
            letterCounts.merge(letter, 1, Integer::sum);
        }

        for (int i = 0; i < word.length(); i++) {
            if (attemptChars[i] == wordChar[i]) {
                result[i] = "exato";
                correctLetters++;
                letterCounts.computeIfPresent(attempt.charAt(i), (k, count) -> (count > 1) ? count - 1 : null);
            }
        }

        for (int i = 0; i < word.length(); i++) {

            if (result[i].equals("exato")) continue;

            if (letterCounts.containsKey(attemptChars[i])) {
                result[i] = "quase";
                letterCounts.computeIfPresent(attemptChars[i], (key, count) -> (count > 1) ? count - 1 : null);

            }

        }

        game.registerAttempt(userId);



        return GuessResult.newBuilder().setIsWinner(correctLetters == word.length())
                .setAcertos(correctLetters)
                .setAttempts(game.getAttemptsUser(userId))
                .addAllValues(Arrays.asList(result))
                .setUserId(userId)
                .build();

    }

    public void joinQueue(Player player) {
        player.setStatus(Status.NA_FILA);
        playerQueue.enqueue(player);
        notifyJoinedQueue(player);
    }

    public boolean isAlredy() {
        return playerQueue.size() % 2 == 0;
    }

    public void notifyOpponent(EnviarPalpiteRequest request, GuessResult result) {
        Game game = matches.get(request.getGameId());
        Player opponent = game.getOpponent(request.getUserId());
        opponent.getSessao().onNext(GameResponse.newBuilder().setOpponentGuess(result).build());
    }
    public void checkGameOver(EnviarPalpiteRequest request, GuessResult result) {
        Game game = matches.get(request.getGameId());
        Player player = game.getPlayer(request.getUserId());
        Player opponent = game.getOpponent(request.getUserId());

        if (result.getIsWinner()) {
            finishGame(game, player, MatchResult.VITORIA, opponent, MatchResult.DERROTA, player.getName());
            return;
        }

        if (!game.hasAttemptsLeft(player.getId()) && !game.hasAttemptsLeft(opponent.getId())) {
            finishGame(game, player, MatchResult.EMPATE, opponent, MatchResult.EMPATE, "");
        }
    }

    private void finishGame(Game game, Player p1, MatchResult r1, Player p2, MatchResult r2, String winnerName) {
        game.setFinished(true);
        notifyGameFinished(p1, r1, game, winnerName);
        notifyGameFinished(p2, r2, game, winnerName);
        p1.setStatus(Status.LIVRE);
        p2.setStatus(Status.LIVRE);
        matches.remove(game.getGameID());
    }

    private void notifyGameFinished(Player player, MatchResult result, Game game, String winnerName) {
        GameFinished finished = GameFinished.newBuilder()
                .setResult(result)
                .setSecretWord(game.getWord())
                .build();

        player.getSessao().onNext(GameResponse.newBuilder().setGameFinished(finished).build());
    }

    private void notifyJoinedQueue(Player player) {
        PlayerJoined playerJoined = PlayerJoined.newBuilder()
                .setMessage(Status.NA_FILA.toString())
                .build();

        GameResponse response = GameResponse.newBuilder()
                .setPlayerJoined(playerJoined)
                .build();

        player.getSessao().onNext(response);
    }

    private void notifyGameStarted(Player player, Player opponent, Game game) {
        GameResponse response = GameResponse.newBuilder()
                .setGameStarted(buildGameStarted(opponent, game.getWord()))
                .build();

        player.getSessao().onNext(response);
    }

    private GameStarted buildGameStarted(Player opponent, String word) {
        return GameStarted.newBuilder()
                .setGameId(1)
                .setOpponentName(opponent.getName())
                .setWordSize(word.length()).build();
    }


}
