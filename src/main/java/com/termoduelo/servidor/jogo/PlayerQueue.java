package com.termoduelo.servidor.jogo;

import com.termoduelo.model.Player;

import java.util.LinkedList;
import java.util.Queue;

public class PlayerQueue {

    private final Queue<Player> waitingPlayers = new LinkedList<>();

    public void enqueue(Player player) {
        waitingPlayers.offer(player);
    }

    public Player dequeue() {
        return waitingPlayers.poll();
    }

    public Player peek() {
        return waitingPlayers.peek();
    }

    public boolean remove(Player player) {
        return waitingPlayers.remove(player);
    }

    public boolean isEmpty() {
        return waitingPlayers.isEmpty();
    }

    public int size() {
        return waitingPlayers.size();
    }
}