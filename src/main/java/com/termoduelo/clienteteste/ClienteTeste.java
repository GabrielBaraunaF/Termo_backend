package com.termoduelo.clienteteste;

import com.termoduelo.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClienteTeste {

    public static void main(String[] args) throws InterruptedException {

        Scanner scanner = new Scanner(System.in);

        CountDownLatch gameStartedLatch = new CountDownLatch(1);
        CountDownLatch streamClosedLatch = new CountDownLatch(1);
        int[] gameId = new int[1];

        ManagedChannel canal = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        TermoDueloGrpc.TermoDueloBlockingStub blockingStub = TermoDueloGrpc.newBlockingStub(canal);
        TermoDueloGrpc.TermoDueloStub asyncStub = TermoDueloGrpc.newStub(canal);

        try {
            System.out.println("escreva seu nome");
            String nome = scanner.nextLine();

            CadastroRequest cadastroRequest = CadastroRequest.newBuilder().setNome(nome).build();
            System.out.println("Canal criado: " + canal);
            CadastroResponse userid = blockingStub.cadastro(cadastroRequest);
            asyncStub.login(LoginRequest.newBuilder().setUserId(userid.getNome()).build(), new StreamObserver<GameResponse>() {
                @Override
                public void onNext(GameResponse value) {
                    System.out.println("[stream] Resposta do servidor: " + value);

                    if (value.hasGameStarted()) {
                        gameId[0] = value.getGameStarted().getGameId();
                        gameStartedLatch.countDown();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("[stream] Erro: " + t);
                    streamClosedLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    System.out.println("[stream] Finalizado pelo servidor");
                    streamClosedLatch.countDown();
                }
            });

            gameStartedLatch.await();

            while (true) {
                System.out.println("digite seu palpite (ou 'sair')");
                String palpite = scanner.nextLine().toUpperCase();

                if (palpite.equals("SAIR")) break;

                try {
                    GuessResult result = blockingStub.enviarPalpite(EnviarPalpiteRequest.newBuilder()
                            .setGameId(gameId[0])
                            .setUserId(userid.getNome())
                            .setPalpite(palpite)
                            .build());

                    System.out.println("[unario] Resultado: " + result.getValuesList());
                    if (result.getIsWinner()) break;
                } catch (StatusRuntimeException e) {
                    System.out.println("[unario] Erro: " + e.getStatus());
                }
            }

            System.out.println("aguardando mensagens do stream...");
            streamClosedLatch.await();
        } finally {
            canal.shutdownNow().awaitTermination(100, TimeUnit.SECONDS);
        }
    }
}
