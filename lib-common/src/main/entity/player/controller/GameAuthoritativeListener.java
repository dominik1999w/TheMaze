package entity.player.controller;

import entity.player.Player;

public interface GameAuthoritativeListener {
    void setNextState(long timestamp, Player player);
}
