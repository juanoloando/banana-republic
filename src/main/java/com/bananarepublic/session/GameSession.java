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
}