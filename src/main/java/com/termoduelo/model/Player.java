package com.termoduelo.model;

import io.grpc.stub.StreamObserver;

public class Player {

    private int id;

    private String name;

    private Status status;

    private StreamObserver sessao;


    public Player(String name, Status status,int id) {
        this.name = name;
        this.status = status;
        this.id = id;
    }

    public Player() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public StreamObserver getSessao() {
        return sessao;
    }

    public void setSessao(StreamObserver sessao) {
        this.sessao = sessao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
