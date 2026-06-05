package com.bananarepublic.controller;

import com.bananarepublic.controller.GameController;
import com.bananarepublic.controller.GameSession;
import engine.GameEngine;
import engine.GameStateImpl;
import model.board.Board;
import model.board.HexCoordinate;
import model.board.StandardMapGenerator;
import model.card.CardDeck;
import model.card.CardFactory;
import model.player.HumanPlayer;
import model.player.Player;
import model.player.PlayerColor;
import model.resource.ResourceBank;
import persistence.SaveData;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory untuk membangun dan mewiring GameEngine + GameController.
 * Dipanggil oleh LobbyController saat game baru dimulai.
 */
public final class GameFactory {

    private GameFactory() {}

    /**
     * Buat GameController lengkap dari GameSession (new game).
     * Urutan: buat players → board → bank → deck → state → engine → controller
     */
    public static GameController build() {
        List<String> names = GameSession.getPlayerNames();

        // ── Players ───────────────────────────────────────────────────
        PlayerColor[] colors = { PlayerColor.YELLOW, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN };
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            players.add(new HumanPlayer(names.get(i), colors[i % colors.length]));
        }

        // ── Board ─────────────────────────────────────────────────────
        Board board = new StandardMapGenerator().generateBoard();

        // ── Robber starts at desert ───────────────────────────────────
        HexCoordinate robberStart = board.getAllTiles().stream()
                .filter(t -> t.getTerrainType() == model.board.TerrainType.DESERT)
                .map(t -> t.getCoordinate())
                .findFirst()
                .orElse(new HexCoordinate(0, 0));

        // ── Bank + Deck ───────────────────────────────────────────────
        ResourceBank bank = new ResourceBank();
        CardDeck deck = new CardDeck(CardFactory.createStandardDeck());
        deck.shuffle();

        // ── Game state + engine ───────────────────────────────────────
        GameStateImpl state = new GameStateImpl(board, players, bank, deck, robberStart);
        GameEngine engine   = new GameEngine(state);

        // ── Bridge layer ──────────────────────────────────────────────
        GameController controller = new GameController(engine);
        engine.addListener(controller);

        return controller;
    }

    /**
     * Buat GameController dari SaveData yang sudah dimuat.
     * Resources, buildings, pipes, dan kartu di-restore ke state engine.
     */
    public static GameController buildFromSave(SaveData saveData) {
        // ── Players dari save ─────────────────────────────────────────
        PlayerColor[] colors = { PlayerColor.YELLOW, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN };
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < saveData.players.size(); i++) {
            SaveData.PlayerData pd = saveData.players.get(i);
            PlayerColor savedColor = (pd.color != null)
                ? PlayerColor.valueOf(pd.color.toUpperCase())
                : colors[i % colors.length];
            HumanPlayer player = new HumanPlayer(pd.name, savedColor);
            // Restore resources
            if (pd.resources != null) {
                pd.resources.forEach((key, val) -> {
                    try {
                        model.resource.ResourceType type =
                            model.resource.ResourceType.valueOf(key.toUpperCase());
                        if (val > 0) player.getInventory().addResource(type, val);
                    } catch (IllegalArgumentException ignored) {}
                });
            }
            // Restore experiment cards to engine inventory so Use Card popup works
            for (int k = 0; k < pd.knightCardsInHand; k++) {
                player.getInventory().addCard(CardFactory.createKnight());
            }
            for (int k = 0; k < pd.progressCardsInHand; k++) {
                // Save format combines RoadBuilding+Monopoly; restore all as RoadBuilding
                player.getInventory().addCard(CardFactory.createRoadBuilding());
            }
            for (int k = 0; k < pd.victoryPointCardsInHand; k++) {
                player.getInventory().addCard(CardFactory.createVictoryPoint());
            }
            for (int k = 0; k < pd.knightCardsPlayed; k++) {
                player.getInventory().incrementKnightCardsPlayed();
            }
            players.add(player);
        }

        // ── Board ─────────────────────────────────────────────────────
        Board board = new StandardMapGenerator().generateBoard();

        // Tempatkan bangunan & pipe dari save ke engine board agar BuildValidator valid
        for (int i = 0; i < saveData.players.size(); i++) {
            SaveData.PlayerData pd = saveData.players.get(i);
            HumanPlayer player = (HumanPlayer) players.get(i);

            if (pd.buildings != null) {
                for (SaveData.BuildingData bd : pd.buildings) {
                    board.getIntersection(bd.intersectionId).ifPresent(inter -> {
                        if (inter.getBuilding().isEmpty()) {
                            model.building.Building bld =
                                "LABORATORY".equalsIgnoreCase(bd.buildingType)
                                    ? new model.building.Laboratory(player.getName())
                                    : new model.building.Settlement(player.getName());
                            inter.placeBuilding(bld);
                            if ("LABORATORY".equalsIgnoreCase(bd.buildingType)) {
                                player.upgradeLaboratory(bd.intersectionId);
                            } else {
                                player.placeSettlement(bd.intersectionId);
                            }
                        }
                    });
                }
            }

            if (pd.pipePathIds != null) {
                for (int pathId : pd.pipePathIds) {
                    board.getPath(pathId).ifPresent(path -> {
                        if (path.isEmpty()) {
                            path.placePipe(new model.building.Pipe(player.getName()));
                            player.placePipe(pathId);
                        }
                    });
                }
            }
        }

        // ── Bank ──────────────────────────────────────────────────────
        ResourceBank bank;
        if (saveData.bank != null) {
            java.util.EnumMap<model.resource.ResourceType, Integer> stock =
                new java.util.EnumMap<>(model.resource.ResourceType.class);
            saveData.bank.forEach((key, val) -> {
                try {
                    stock.put(model.resource.ResourceType.valueOf(key.toUpperCase()), val);
                } catch (IllegalArgumentException ignored) {}
            });
            bank = new ResourceBank(stock);
        } else {
            bank = new ResourceBank();
        }

        // ── Deck ──────────────────────────────────────────────────────
        CardDeck deck = new CardDeck(CardFactory.createStandardDeck());
        deck.shuffle();

        // ── Robber ────────────────────────────────────────────────────
        HexCoordinate robberPos = new HexCoordinate(saveData.robberQ, saveData.robberR);

        // ── State + engine ────────────────────────────────────────────
        GameStateImpl state = new GameStateImpl(board, players, bank, deck, robberPos);

        // Restore phase dari save, dengan aturan:
        // - Setup phase → pertahankan (belum selesai setup)
        // - GAME_OVER   → pertahankan
        // - Semua lainnya (TRADE_BUILD, ROBBER_*) → reset ke ROLL_DICE
        //   agar pemain bisa langsung lempar dadu setelah load
        engine.TurnPhase phaseToRestore = engine.TurnPhase.ROLL_DICE; // default
        if (saveData.currentPhase != null) {
            try {
                engine.TurnPhase savedPhase = engine.TurnPhase.valueOf(
                    saveData.currentPhase.trim().toUpperCase());
                if (savedPhase == engine.TurnPhase.SETUP_PLACE_FIRST
                 || savedPhase == engine.TurnPhase.SETUP_PLACE_SECOND
                 || savedPhase == engine.TurnPhase.GAME_OVER) {
                    phaseToRestore = savedPhase;
                }
                // Semua fase lain (TRADE_BUILD, ROBBER_*, ROLL_DICE) → ROLL_DICE
            } catch (IllegalArgumentException ignored) {}
        }
        state.setCurrentPhase(phaseToRestore);
        if (saveData.currentPlayerIndex >= 0
                && saveData.currentPlayerIndex < players.size()) {
            state.setCurrentPlayerIndex(saveData.currentPlayerIndex);
        }

        // Restore special card holders and lengths
        if (saveData.longestRoadHolder != null) {
            players.stream()
                .filter(p -> p.getName().equals(saveData.longestRoadHolder))
                .findFirst()
                .ifPresent(p -> state.grantSpecialCard(model.player.SpecialCard.LONGEST_ROAD, p));
        }
        if (saveData.largestArmyHolder != null) {
            players.stream()
                .filter(p -> p.getName().equals(saveData.largestArmyHolder))
                .findFirst()
                .ifPresent(p -> state.grantSpecialCard(model.player.SpecialCard.LARGEST_ARMY, p));
        }
        if (saveData.currentLongestRoadLength > 0) state.setLongestRoadLength(saveData.currentLongestRoadLength);
        if (saveData.currentLargestArmySize > 0) state.setLargestArmySize(saveData.currentLargestArmySize);

        GameEngine engine = new GameEngine(state);
        GameController controller = new GameController(engine);
        engine.addListener(controller);

        return controller;
    }
}
