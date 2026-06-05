package com.bananarepublic.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bananarepublic.controller.GameController;
import persistence.SaveData;

public class GameSession {
    private static final List<String> playerNames = new ArrayList<>();
    private static final Map<Integer, String> playerBotPluginPaths = new HashMap<>();
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

    public static void setPlayerBotPluginPaths(Map<Integer, String> botPluginPaths) {
        playerBotPluginPaths.clear();
        playerBotPluginPaths.putAll(botPluginPaths);
    }

    public static Map<Integer, String> getPlayerBotPluginPaths() {
        return new HashMap<>(playerBotPluginPaths);
    }

    public static void clearPlayerBotPluginPaths() {
        playerBotPluginPaths.clear();
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

    // ── GameController (bridge layer) ─────────────────────────────────

    private static GameController gameController;

    public static void setGameController(GameController gc)  { gameController = gc; }
    public static GameController getGameController()         { return gameController; }
    public static void clearGameController()                 { gameController = null; }

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
