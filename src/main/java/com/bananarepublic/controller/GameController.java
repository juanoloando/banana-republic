package com.bananarepublic.controller;

import engine.*;
import javafx.application.Platform;
import model.board.Board;
import model.board.BoardPath;
import model.board.HexCoordinate;
import model.board.Intersection;
import model.building.Laboratory;
import model.building.Settlement;
import model.card.ExperimentCard;
import model.exception.InvalidBuildException;
import model.player.Player;
import model.resource.Bank;
import model.resource.ResourceType;

import java.util.*;

/**
 * Bridge layer antara UI dan GameEngine.
 * UI controller hanya boleh memanggil GameController — tidak langsung ke GameEngine.
 *
 * Extends GameEngineAdapter (diizinkan oleh CLAUDE.md) untuk menerima event engine
 * dan meneruskannya ke UI via Platform.runLater().
 */
public class GameController extends GameEngineAdapter {

    private final GameEngine engine;
    private final BuildValidator validator = new BuildValidator();
    private final VictoryManager victoryManager = new VictoryManager();

    // UI callback interface — diset oleh GameRevisionController setelah initialize
    private GameViewCallback uiCallback;

    public GameController(GameEngine engine) {
        this.engine = Objects.requireNonNull(engine, "GameEngine must not be null");
    }

    public void setUiCallback(GameViewCallback callback) {
        this.uiCallback = callback;
    }

    // ── Action delegates (dipanggil UI) ──────────────────────────────

    /** Lempar dadu normal. Engine akan mendistribusikan resource dan ubah phase. */
    public DiceResult rollDice() {
        return engine.rollDice();
    }

    /** Set dadu manual untuk testing. */
    public void setManualDice(int die1, int die2) {
        engine.setManualDice(die1, die2);
    }

    /** Bangun outpost (settlement) baru di intersection. */
    public void buildSettlement(int intersectionId) {
        engine.buildSettlement(intersectionId);
    }

    /** Upgrade outpost ke lab. */
    public void upgradeLaboratory(int intersectionId) {
        engine.upgradeLaboratory(intersectionId);
    }

    /** Bangun pipe di path (fase TRADE_BUILD). */
    public void buildPipe(int pathId) {
        engine.buildPipe(pathId);
    }

    /** Bangun pipe saat setup — harus adjacent ke settlement baru. */
    public void buildPipeSetup(int pathId, int adjacentSettlementIntersectionId) {
        engine.buildPipeSetup(pathId, adjacentSettlementIntersectionId);
    }

    public void endTurn()                                               { engine.endTurn(); }
    public void moveRobber(HexCoordinate position)                      { engine.moveRobber(position); }
    public void stealFromPlayer(Player target)                          { engine.stealFromPlayer(target); }
    public void discardResources(Player p, Map<ResourceType,Integer> d) { engine.discardResources(p, d); }
    public ExperimentCard buyExperimentCard()                           { return engine.buyExperimentCard(); }
    public void playCard(ExperimentCard card)                           { engine.playCard(card); }
    public void applyMonopolyCard(model.card.MonopolyCard card, ResourceType r) {
        engine.applyMonopolyCard(card, r);
    }

    // ── State read ────────────────────────────────────────────────────

    public TurnPhase getCurrentPhase()       { return engine.getState().getCurrentPhase(); }
    public Player     getActivePlayer()      { return engine.getState().getActivePlayer(); }
    public Board      getBoard()             { return engine.getState().getBoard(); }
    public Bank       getBank()              { return engine.getState().getBank(); }
    public List<Player> getAllPlayers()      { return engine.getState().getAllPlayers(); }
    public int        getCurrentPlayerIdx()  { return engine.getState().getCurrentPlayerIndex(); }
    public HexCoordinate getRobberPosition(){ return engine.getState().getRobberPosition(); }
    public boolean    isSetupPhase()         { return engine.getState().isSetupPhase(); }
    public boolean    isGameOver()           { return engine.getState().isGameOver(); }
    public Optional<Player> getWinner()      { return engine.getState().getWinner(); }

    public List<Player> getStealCandidates()           { return engine.getStealCandidates(); }
    public List<Player> getPlayersRequiredToDiscard()  { return engine.getPlayersRequiredToDiscard(); }
    public int computeDiscardCount(Player p)           { return engine.computeDiscardCount(p); }
    public boolean isCardPlayedThisTurn()              { return engine.isCardPlayedThisTurn(); }
    public int getFreeRoadsRemaining()                 { return engine.getFreeRoadsRemaining(); }
    public DiceResult getLastDiceResult()              { return engine.getLastDiceResult(); }

    /** Save current game state to JSON file. Returns true on success. */
    public boolean saveGame(java.io.File file) {
        try {
            engine.saveGame(file);
            return true;
        } catch (Exception e) {
            System.err.println("Save failed: " + e.getMessage());
            return false;
        }
    }

    /** Inject a card plugin JAR into the live deck at runtime. */
    public void injectCardPlugin(java.io.File jarFile) throws plugin.PluginLoadException {
        engine.injectCardPlugin(jarFile);
    }

    /** Hitung panjang jalan terpanjang (longest pipe chain) milik player. */
    public int computeLongestRoad(Player player) {
        return engine.computeLongestRoad(player);
    }

    /** Hitung VP player dari buildings + VP cards + special cards. */
    public int computeVictoryPoints(Player player) {
        return victoryManager.computeVictoryPoints(player, getBoard());
    }

    public int getVictoryThreshold() {
        return victoryManager.getVictoryThreshold();
    }

    public void confirmVictory(Player winner) {
        engine.confirmVictory(winner);
    }

    // ── Validation helpers — pakai BuildValidator ─────────────────────

    /**
     * Intersection kosong yang lolos distance rule + settlement connectivity.
     * Distance rule berlaku sejak setup (spesifikasi eksplisit).
     */
    public Set<Integer> getValidSettlementIds(Player player) {
        Set<Integer> result = new HashSet<>();
        Board board = getBoard();
        boolean isSetup = isSetupPhase();
        for (Intersection inter : board.getAllIntersections()) {
            try {
                validator.validateSettlementPlacement(board, player, inter.getId(), isSetup);
                result.add(inter.getId());
            } catch (InvalidBuildException ignored) {
                // not valid
            }
        }
        return result;
    }

    /**
     * Intersection yang SUDAH ada outpost milik player ini dan bisa diupgrade ke lab.
     */
    public Set<Integer> getValidLabIds(Player player) {
        Set<Integer> result = new HashSet<>();
        Board board = getBoard();
        for (Intersection inter : board.getAllIntersections()) {
            try {
                validator.validateLaboratoryUpgrade(board, player, inter.getId());
                result.add(inter.getId());
            } catch (InvalidBuildException ignored) {
                // not valid
            }
        }
        return result;
    }

    /**
     * Path kosong yang terhubung ke jaringan pipe/settlement player.
     * Untuk setup: hanya path adjacent ke intersection tertentu.
     */
    public Set<Integer> getValidPipeIds(Player player) {
        return getValidPipeIds(player, null);
    }

    public Set<Integer> getValidPipeIds(Player player, Integer setupIntersectionId) {
        Set<Integer> result = new HashSet<>();
        Board board = getBoard();
        boolean isSetup = isSetupPhase();
        for (BoardPath path : board.getAllPaths()) {
            try {
                validator.validatePipePlacement(board, player, path.getId(), isSetup, setupIntersectionId);
                result.add(path.getId());
            } catch (InvalidBuildException ignored) {
                // not valid
            }
        }
        return result;
    }

    // ── GameEngineListener callbacks → forward to UI ──────────────────

    @Override
    public void onPhaseChanged(TurnPhase newPhase) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onPhaseChanged(newPhase); });
    }

    @Override
    public void onDiceRolled(DiceResult result) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onDiceRolled(result); });
    }

    @Override
    public void onTurnChanged(Player newActivePlayer) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onTurnChanged(newActivePlayer); });
    }

    @Override
    public void onBuildingPlaced(Player player, int intersectionId, String buildingType) {
        Platform.runLater(() -> {
            if (uiCallback != null) uiCallback.onBuildingPlaced(player, intersectionId, buildingType);
        });
    }

    @Override
    public void onPipePlaced(Player player, int pathId) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onPipePlaced(player, pathId); });
    }

    @Override
    public void onResourcesDistributed(Player activePlayer) {
        Platform.runLater(() -> {
            if (uiCallback != null) uiCallback.onResourcesDistributed(activePlayer);
        });
    }

    @Override
    public void onRobberMoved(HexCoordinate newPosition) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onRobberMoved(newPosition); });
    }

    @Override
    public void onResourcesDiscarded(Player player) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onResourcesDiscarded(player); });
    }

    @Override
    public void onResourceStolen(Player thief, Player victim, ResourceType stolenResource) {
        Platform.runLater(() -> {
            if (uiCallback != null) uiCallback.onResourceStolen(thief, victim, stolenResource);
        });
    }

    @Override
    public void onCardBought(Player player, ExperimentCard card) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onCardBought(player, card); });
    }

    @Override
    public void onCardPlayed(Player player, ExperimentCard card) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onCardPlayed(player, card); });
    }

    @Override
    public void onKnightPlayed(Player player) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onKnightPlayed(player); });
    }

    @Override
    public void onTimerTick(int secondsRemaining) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onTimerTick(secondsRemaining); });
    }

    @Override
    public void onTimerExpired(Player playerWhoTimedOut) {
        Platform.runLater(() -> {
            if (uiCallback != null) uiCallback.onTimerExpired(playerWhoTimedOut);
        });
    }

    @Override
    public void onGameWon(Player winner) {
        Platform.runLater(() -> { if (uiCallback != null) uiCallback.onGameWon(winner); });
    }
}
