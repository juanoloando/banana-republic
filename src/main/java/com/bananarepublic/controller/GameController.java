package com.bananarepublic.controller;

import engine.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import model.board.Board;
import model.board.BoardPath;
import model.board.HexCoordinate;
import model.board.Intersection;
import model.board.TerrainType;
import model.building.BuildCost;
import model.card.ExperimentCard;
import model.exception.InvalidBuildException;
import model.player.Player;
import model.resource.Bank;
import model.resource.ResourceType;
import plugin.BotPlayer;

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
        // If the first active player is a bot, kick off its action after the UI is ready.
        Platform.runLater(() -> scheduleBotActionIfNeeded(engine.getState().getCurrentPhase()));
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
        Platform.runLater(() -> {
            // Auto-discard bots BEFORE the UI queries getPlayersRequiredToDiscard().
            if (newPhase == TurnPhase.ROBBER_DISCARD) {
                autoBotDiscardAll();
            }
            // If autoBotDiscardAll advanced the phase (all humans done too) skip the stale callback.
            TurnPhase currentPhase = engine.getState().getCurrentPhase();
            if (currentPhase == newPhase) {
                // Bots drive interactive placement phases automatically.
                // Skip forwarding those phases to the UI so the tile-pick overlay,
                // robber move circles, and settlement-select mode never appear for bots.
                boolean activeIsBot = engine.getState().getActivePlayer() instanceof BotPlayer;
                boolean skipInteractiveUi = activeIsBot && isInteractivePlacementPhase(newPhase);
                if (uiCallback != null && !skipInteractiveUi) {
                    uiCallback.onPhaseChanged(newPhase);
                }
                scheduleBotActionIfNeeded(currentPhase);
            }
            // When the phase changed inside autoBotDiscardAll the new phase's own
            // Platform.runLater (queued by notifyListeners) handles UI + bot scheduling.
        });
    }

    /**
     * Returns true for phases that put the UI into an interactive "click to place" mode.
     * These are driven automatically for bots, so the UI should not enter those modes.
     */
    private static boolean isInteractivePlacementPhase(TurnPhase phase) {
        return phase == TurnPhase.ROBBER_MOVE
            || phase == TurnPhase.SETUP_PLACE_FIRST
            || phase == TurnPhase.SETUP_PLACE_SECOND;
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

    // ── Bot automation ────────────────────────────────────────────────

    private static final double BOT_ACTION_DELAY_SEC = 1.2;

    /** Schedule the active bot's action for the given phase after a short visual delay. */
    private void scheduleBotActionIfNeeded(TurnPhase phase) {
        if (engine.getState().isGameOver()) return;
        Player active = engine.getState().getActivePlayer();
        if (!(active instanceof BotPlayer)) return;
        BotPlayer bot = (BotPlayer) active;
        PauseTransition delay = new PauseTransition(Duration.seconds(BOT_ACTION_DELAY_SEC));
        delay.setOnFinished(e -> executeBotPhaseAction(bot, phase));
        delay.play();
    }

    /** Dispatch the correct action for the given phase; guards stale callbacks. */
    private void executeBotPhaseAction(BotPlayer bot, TurnPhase phase) {
        if (engine.getState().isGameOver()) return;
        if (!engine.getState().getActivePlayer().equals(bot)) return;
        if (engine.getState().getCurrentPhase() != phase) return;
        try {
            switch (phase) {
                case SETUP_PLACE_FIRST:
                case SETUP_PLACE_SECOND:
                    executeBotSetup(bot);
                    break;
                case ROLL_DICE:
                    engine.rollDice();
                    break;
                case ROBBER_MOVE:
                    executeBotRobberMove(bot);
                    break;
                case TRADE_BUILD:
                    executeBotTradeBuild(bot);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println("[BOT] " + bot.getName() + " error in " + phase + ": " + e.getMessage());
        }
    }

    /** Place a random valid settlement then a random adjacent pipe during setup. */
    private void executeBotSetup(BotPlayer bot) {
        Set<Integer> validSettlements = getValidSettlementIds(bot);
        if (validSettlements.isEmpty()) return;
        List<Integer> settlementList = new ArrayList<>(validSettlements);
        int intersectionId = settlementList.get((int)(Math.random() * settlementList.size()));
        engine.buildSettlement(intersectionId);

        Set<Integer> validPipes = getValidPipeIds(bot, intersectionId);
        if (validPipes.isEmpty()) return;
        List<Integer> pipeList = new ArrayList<>(validPipes);
        int pipeId = pipeList.get((int)(Math.random() * pipeList.size()));
        engine.buildPipeSetup(pipeId, intersectionId);
    }

    /** Move robber to a non-desert tile, then steal from the richest adjacent player. */
    private void executeBotRobberMove(BotPlayer bot) {
        HexCoordinate current = engine.getState().getRobberPosition();
        HexCoordinate target = engine.getState().getBoard().getAllTiles().stream()
                .filter(t -> !t.getCoordinate().equals(current)
                          && t.getTerrainType() != TerrainType.DESERT)
                .map(t -> t.getCoordinate())
                .findFirst()
                .orElseGet(() -> engine.getState().getBoard().getAllTiles().stream()
                        .filter(t -> !t.getCoordinate().equals(current))
                        .map(t -> t.getCoordinate())
                        .findFirst()
                        .orElse(current));
        engine.moveRobber(target);

        List<Player> candidates = engine.getStealCandidates();
        if (!candidates.isEmpty()) {
            Player richest = candidates.stream()
                    .max(Comparator.comparingInt(Player::getTotalResourceCount))
                    .orElse(candidates.get(0));
            engine.stealFromPlayer(richest);
        }
    }

    /** Run the bot's resource strategy, then spend resources on builds, then end the turn. */
    private void executeBotTradeBuild(BotPlayer bot) {
        engine.stopTradeBuildTimer();

        // 1. Strategy: gather resources via bank trades (does NOT auto-play/buy any cards).
        try {
            bot.executeTurn(engine.getState(), engine);
        } catch (Exception e) {
            System.err.println("[BOT] " + bot.getName() + " strategy error: " + e.getMessage());
        }

        // 2. Spend gathered resources on actual builds (lab > settlement > pipe).
        //    Cards are intentionally excluded — bots do NOT auto-play experiment cards.
        if (engine.getState().getCurrentPhase() == TurnPhase.TRADE_BUILD
                && !engine.getState().isGameOver()) {
            tryBotBuildActions(bot);
        }

        // 3. End turn.
        if (engine.getState().getCurrentPhase() == TurnPhase.TRADE_BUILD
                && !engine.getState().isGameOver()) {
            engine.endTurn();
        }
    }

    /**
     * Attempt builds in priority order: lab upgrade → new settlement → pipe.
     * Loops until nothing affordable/valid remains, so one TRADE_BUILD phase can place
     * multiple structures when the bot has sufficient resources.
     * Experiment-card purchasing is intentionally omitted to prevent auto-use of cards.
     */
    private void tryBotBuildActions(BotPlayer bot) {
        int guard = 20;
        while (guard-- > 0
               && engine.getState().getCurrentPhase() == TurnPhase.TRADE_BUILD
               && !engine.getState().isGameOver()) {
            if (tryBotUpgradeLab(bot))   continue;
            if (tryBotBuildSettlement(bot)) continue;
            if (tryBotBuildPipe(bot))    continue;
            break;
        }
    }

    private boolean tryBotUpgradeLab(BotPlayer bot) {
        if (!bot.canAfford(BuildCost.LABORATORY.getAllCosts())) return false;
        Set<Integer> ids = getValidLabIds(bot);
        if (ids.isEmpty()) return false;
        try { engine.upgradeLaboratory(ids.iterator().next()); return true; }
        catch (Exception e) { return false; }
    }

    private boolean tryBotBuildSettlement(BotPlayer bot) {
        if (!bot.canAfford(BuildCost.SETTLEMENT.getAllCosts())) return false;
        Set<Integer> ids = getValidSettlementIds(bot);
        if (ids.isEmpty()) return false;
        List<Integer> list = new ArrayList<>(ids);
        try {
            engine.buildSettlement(list.get((int)(Math.random() * list.size())));
            return true;
        } catch (Exception e) { return false; }
    }

    private boolean tryBotBuildPipe(BotPlayer bot) {
        if (!bot.canAfford(BuildCost.PIPE.getAllCosts())) return false;
        Set<Integer> ids = getValidPipeIds(bot);
        if (ids.isEmpty()) return false;
        try { engine.buildPipe(ids.iterator().next()); return true; }
        catch (Exception e) { return false; }
    }

    /** Auto-discard excess resources for every bot that must discard due to a 7 roll. */
    private void autoBotDiscardAll() {
        List<Player> toDiscard = new ArrayList<>(engine.getPlayersRequiredToDiscard());
        for (Player player : toDiscard) {
            if (!(player instanceof BotPlayer)) continue;
            BotPlayer bot = (BotPlayer) player;
            int count = engine.computeDiscardCount(bot);
            if (count <= 0) continue;
            try {
                engine.discardResources(bot, makeBotDiscardMap(bot, count));
            } catch (Exception e) {
                System.err.println("[BOT] " + bot.getName() + " discard error: " + e.getMessage());
            }
        }
    }

    /** Build a discard map: drop the most-abundant resources first. */
    private Map<ResourceType, Integer> makeBotDiscardMap(BotPlayer bot, int count) {
        Map<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        int remaining = count;
        List<ResourceType> sorted = new ArrayList<>(Arrays.asList(ResourceType.values()));
        sorted.sort((a, b) -> bot.getResourceCount(b) - bot.getResourceCount(a));
        for (ResourceType type : sorted) {
            if (remaining <= 0) break;
            int have    = bot.getResourceCount(type);
            int discard = Math.min(have, remaining);
            if (discard > 0) {
                map.put(type, discard);
                remaining -= discard;
            }
        }
        return map;
    }
}
