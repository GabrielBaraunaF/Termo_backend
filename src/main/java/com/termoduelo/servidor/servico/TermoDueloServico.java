package com.termoduelo.servidor.servico;

import com.termoduelo.model.Player;
import com.termoduelo.model.Status;
import com.termoduelo.proto.*;
import com.termoduelo.servidor.jogo.GameService;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;


public class TermoDueloServico extends TermoDueloGrpc.TermoDueloImplBase {

    Map<Integer, Player> players = new HashMap();

    int proximoNumero = 1;

    GameService gameService = new GameService();

    @Override
    public void login(LoginRequest request, StreamObserver<GameResponse> responseObserver) {
        Player playerExits = players.get(request.getUserId());
        playerExits.setSessao(responseObserver);
        gameService.joinQueue(playerExits);

        if (gameService.isAlredy()) {
            gameService.startMatch();
        }

    }

    @Override
    public void cadastro(CadastroRequest request, StreamObserver<CadastroResponse> responseObserver) {
        Player newPlayer = new Player(request.getNome(), Status.LIVRE,proximoNumero);
        players.put(proximoNumero, newPlayer);

        responseObserver.onNext(CadastroResponse.newBuilder()
                .setNome(proximoNumero)
                .build());

        proximoNumero++;
        responseObserver.onCompleted();
    }

    @Override
    public void enviarPalpite(EnviarPalpiteRequest request, StreamObserver<GuessResult> responseObserver) {
        GuessResult result = gameService.submitGuess(request);
        responseObserver.onNext(result);
        responseObserver.onCompleted();
        gameService.notifyOpponent(request, result);
        gameService.checkGameOver(request,result);

    }
}
