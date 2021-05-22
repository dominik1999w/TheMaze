package service;

import java.util.Map;
import java.util.UUID;

import lib.map.Position;

public interface StartGameHandler {
    void initializeGame(int mapLength, int seed, Map<UUID, Position> positions);
}
