package model.card;

import java.util.HashMap;
import java.util.Map;

public class UsedCardTracker {
    private final Map<Integer, Boolean> discoveryCardUsedThisTurn = new HashMap<>();
    private final Map<Integer, Integer> playedKnightCount = new HashMap<>();

    public boolean hasPlayedDiscoveryCardThisTurn(int playerIndex) {
        return discoveryCardUsedThisTurn.getOrDefault(playerIndex, false);
    }

    public void markDiscoveryCardPlayedThisTurn(int playerIndex) {
        discoveryCardUsedThisTurn.put(playerIndex, true);
    }

    public void resetDiscoveryCardFlagForPlayer(int playerIndex) {
        discoveryCardUsedThisTurn.put(playerIndex, false);
    }

    public int getPlayedKnightCount(int playerIndex) {
        return playedKnightCount.getOrDefault(playerIndex, 0);
    }

    public void setPlayedKnightCount(int playerIndex, int count) {
        playedKnightCount.put(playerIndex, Math.max(0, count));
    }

    public void incrementKnightCount(int playerIndex) {
        playedKnightCount.put(playerIndex, getPlayedKnightCount(playerIndex) + 1);
    }
}

