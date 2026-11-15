package com.termoduelo.servidor;

import com.termoduelo.servidor.servico.TermoDueloServico;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Main {

    private static final int PORTA = 50051;

    public static void main(String[] args) throws Exception {
        Server servidor = ServerBuilder.forPort(PORTA)
                .addService(new TermoDueloServico())
                .build()
                .start();

        System.out.println("Servidor iniciado na porta " + PORTA);
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::shutdown));
        servidor.awaitTermination();
    }
}
