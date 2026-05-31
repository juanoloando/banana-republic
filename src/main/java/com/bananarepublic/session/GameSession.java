package com.bananarepublic.session;

import java.util.ArrayList;
import java.util.List;

import persistence.SaveData;

public class GameSession {
    private static final List<String> playerNames = new ArrayList<>();
    private static SaveData loadedSaveData;

    public static void setPlayerNames(List<String> names) {
        playerNames.clear();
        playerNames.addAll(names);
    }

    public static List<String> getPlayerNames() {
        return new ArrayList<>(playerNames);
    }

    public static int getPlayerCount() {
        return playerNames.size();
    }

    public static void setLoadedSaveData(SaveData saveData) {
        loadedSaveData = saveData;
    }

    public static SaveData getLoadedSaveData() {
        return loadedSaveData;
    }

    public static boolean hasLoadedSaveData() {
        return loadedSaveData != null;
    }

    public static void clearLoadedSaveData() {
        loadedSaveData = null;
    }

    // ── Timer pause/resume across scene switches ──────────────────────

    /** -1 = no paused timer; >= 0 = seconds remaining when game was left */
    private static int pausedTimerSeconds = -1;
    private static int pausedActivePlayerIndex = 0;

    public static void pauseGameTimer(int secondsRemaining, int activePlayerIndex) {
        pausedTimerSeconds      = secondsRemaining;
        pausedActivePlayerIndex = activePlayerIndex;
    }

    public static boolean hasPausedGameTimer() {
        return pausedTimerSeconds >= 0;
    }

    public static int getPausedTimerSeconds() {
        return pausedTimerSeconds;
    }

    public static int getPausedActivePlayerIndex() {
        return pausedActivePlayerIndex;
    }

    public static void clearPausedGameTimer() {
        pausedTimerSeconds      = -1;
        pausedActivePlayerIndex = 0;
    }
}