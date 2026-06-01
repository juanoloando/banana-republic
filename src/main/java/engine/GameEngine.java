package engine;

import engine.phase.SetupPhaseHandler;
import engine.phase.RollPhaseHandler;
import engine.phase.TradeBuildPhaseHandler;
import model.board.BoardPath;
import model.board.HexCoordinate;
import model.board.Intersection;
import model.building.Laboratory;
import model.building.Pipe;
import model.building.Settlement;
import model.card.ExperimentCard;
import model.card.KnightCard;
import model.card.MonopolyCard;
import model.card.RoadBuildingCard;
import model.card.VictoryPointCard;
import model.exception.InvalidBuildException;
import model.player.HumanPlayer;
import model.player.Player;
import model.player.SpecialCard;
import model.resource.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class GameEngine {

    private final GameStateImpl state;
    private final Dice dice;
    private final BuildValidator buildValidator;
    private final ResourceDistributor resourceDistributor;
    private final RobberManager robberManager;
    private final VictoryManager victoryManager;
    private final RoadManager roadManager;
    private final TurnTimer turnTimer;

    private final SetupPhaseHandler setupPhaseHandler;
    private final RollPhaseHandler rollPhaseHandler;
    private final TradeBuildPhaseHandler tradeBuildPhaseHandler;

    private DiceResult lastDiceResult;
    private boolean cardPlayedThisTurn;
    private ExperimentCard cardBoughtThisTurn;
    private int freeRoadsRemaining;
    private boolean diceRolledThisTurn;
    private int lastPlacedSettlementId;

    private final List<GameEngineListener> listeners;

    public GameEngine(GameStateImpl state) {
        assert state != null : "GameState must not be null";

        this.state = state;
        this.dice = new Dice();
        this.buildValidator = new BuildValidator();
        this.resourceDistributor = new ResourceDistributor();
        this.robberManager = new RobberManager();
        this.victoryManager = new VictoryManager();
        this.roadManager = new RoadManager();
        this.turnTimer = TurnTimer.createWithJavaFxDispatch(
                this::onTurnTimerExpired,
                this::onTurnTimerTick
        );
        this.setupPhaseHandler = new SetupPhaseHandler();
        this.rollPhaseHandler = new RollPhaseHandler();
        this.tradeBuildPhaseHandler = new TradeBuildPhaseHandler();
        this.lastDiceResult = null;
        this.cardPlayedThisTurn = false;
        this.cardBoughtThisTurn = null;
        this.freeRoadsRemaining = 0;
        this.diceRolledThisTurn = false;
        this.lastPlacedSettlementId = -1;
        this.listeners = new ArrayList<>();
    }

    public void addListener(GameEngineListener listener) {
        assert listener != null : "Listener must not be null";
        listeners.add(listener);
    }

    public void removeListener(GameEngineListener listener) {
        listeners.remove(listener);
    }

    public GameState getState() {
        return state;
    }

    public void setManualDice(int die1, int die2) {
        dice.setManualResult(die1, die2);
    }

    public DiceResult rollDice() {
        assert state.getCurrentPhase() == TurnPhase.ROLL_DICE
                : "rollDice() called outside ROLL_DICE phase, current phase: "
                  + state.getCurrentPhase();
        assert !state.isGameOver() : "rollDice() called after game is over";

        int[] rolled = dice.roll();
        lastDiceResult = new DiceResult(rolled[0], rolled[1]);
        diceRolledThisTurn = true;

        notifyListeners(l -> l.onDiceRolled(lastDiceResult));
        rollPhaseHandler.handle(state, this);
        notifyListeners(l -> l.onPhaseChanged(state.getCurrentPhase()));

        return lastDiceResult;
    }

    public void buildSettlement(int intersectionId) {
        assert state.getCurrentPhase() == TurnPhase.TRADE_BUILD
                || state.getCurrentPhase() == TurnPhase.SETUP_PLACE_FIRST
                || state.getCurrentPhase() == TurnPhase.SETUP_PLACE_SECOND
                : "buildSettlement() called in invalid phase: " + state.getCurrentPhase();
        assert !state.isGameOver() : "buildSettlement() called after game is over";

        Player player = state.getActivePlayer();
        boolean isSetup = state.isSetupPhase();

        buildValidator.validateSettlementPlacement(state.getBoard(), player,
                intersectionId, isSetup);

        if (!isSetup) {
            if (!player.canAfford(model.building.BuildCost.SETTLEMENT.getAllCosts())) {
                throw new InvalidBuildException("Not enough resources to build Watch Post.");
            }
            payBuildCost(player, model.building.BuildCost.SETTLEMENT);
        }

        Settlement settlement = new Settlement(player.getName());
        Intersection intersection = state.getBoard().getIntersection(intersectionId)
                .orElseThrow(() -> new InvalidBuildException(
                        "Intersection not found: " + intersectionId));
        intersection.placeBuilding(settlement);

        if (player instanceof HumanPlayer) {
            ((HumanPlayer) player).placeSettlement(intersectionId);
        }

        lastPlacedSettlementId = intersectionId;
        notifyListeners(l -> l.onBuildingPlaced(player, intersectionId, "Settlement"));

        checkVictoryCondition();
    }

    public void buildPipe(int pathId) {
        assert state.getCurrentPhase() == TurnPhase.TRADE_BUILD
                : "buildPipe() called outside TRADE_BUILD phase: " + state.getCurrentPhase();
        assert !state.isGameOver() : "buildPipe() called after game is over";

        Player player = state.getActivePlayer();
        boolean isFreeRoad = freeRoadsRemaining > 0;

        buildValidator.validatePipePlacement(state.getBoard(), player, pathId, false, null);

        if (!isFreeRoad) {
            if (!player.canAfford(model.building.BuildCost.PIPE.getAllCosts())) {
                throw new InvalidBuildException("Not enough resources to build Pipe.");
            }
            payBuildCost(player, model.building.BuildCost.PIPE);
        }

        if (isFreeRoad) {
            freeRoadsRemaining--;
        }

        Pipe pipe = new Pipe(player.getName());
        BoardPath path = state.getBoard().getPath(pathId)
                .orElseThrow(() -> new InvalidBuildException("Path not found: " + pathId));
        path.placePipe(pipe);

        if (player instanceof HumanPlayer) {
            ((HumanPlayer) player).placePipe(pathId);
        }

        roadManager.updateLongestRoad(state, player);
        notifyListeners(l -> l.onPipePlaced(player, pathId));
        checkVictoryCondition();
    }

    public void buildPipeSetup(int pathId, int adjacentSettlementIntersectionId) {
        assert state.isSetupPhase()
                : "buildPipeSetup() called outside setup phase";

        Player player = state.getActivePlayer();

        buildValidator.validatePipePlacement(state.getBoard(), player, pathId,
                true, adjacentSettlementIntersectionId);

        Pipe pipe = new Pipe(player.getName());
        BoardPath path = state.getBoard().getPath(pathId)
                .orElseThrow(() -> new InvalidBuildException("Path not found: " + pathId));
        path.placePipe(pipe);

        if (player instanceof HumanPlayer) {
            ((HumanPlayer) player).placePipe(pathId);
        }

        notifyListeners(l -> l.onPipePlaced(player, pathId));

        if (state.getCurrentPhase() == TurnPhase.SETUP_PLACE_SECOND) {
            resourceDistributor.distributeInitialResources(
                    state.getBoard(), player,
                    lastPlacedSettlementId,
                    state.getBank()
            );
            notifyListeners(l -> l.onResourcesDistributed(player));
        }

        setupPhaseHandler.handle(state, this);
        notifyListeners(l -> l.onPhaseChanged(state.getCurrentPhase()));
        notifyListeners(l -> l.onTurnChanged(state.getActivePlayer()));
    }

    public void upgradeLaboratory(int intersectionId) {
        assert state.getCurrentPhase() == TurnPhase.TRADE_BUILD
                : "upgradeLaboratory() called outside TRADE_BUILD phase";
        assert !state.isGameOver() : "upgradeLaboratory() called after game is over";

        Player player = state.getActivePlayer();

        buildValidator.validateLaboratoryUpgrade(state.getBoard(), player, intersectionId);
        payBuildCost(player, model.building.BuildCost.LABORATORY);

        Laboratory lab = new Laboratory(player.getName());
        Intersection intersection = state.getBoard().getIntersection(intersectionId)
                .orElseThrow(() -> new InvalidBuildException(
                        "Intersection not found: " + intersectionId));
        intersection.upgradeBuilding(lab);

        if (player instanceof HumanPlayer) {
            ((HumanPlayer) player).upgradeLaboratory(intersectionId);
        }

        notifyListeners(l -> l.onBuildingPlaced(player, intersectionId, "Laboratory"));
        checkVictoryCondition();
    }

    public ExperimentCard buyExperimentCard() {
        assert state.getCurrentPhase() == TurnPhase.TRADE_BUILD
                : "buyExperimentCard() called outside TRADE_BUILD phase";
        assert !state.isGameOver() : "buyExperimentCard() called after game is over";
        assert !state.getDeck().isEmpty() : "Cannot buy card: deck is empty";

        Player player = state.getActivePlayer();
        payBuildCost(player, model.building.BuildCost.EXPERIMENT_CARD);

        ExperimentCard card = state.getDeck().draw()
                .orElseThrow(() -> new InvalidBuildException("Deck is empty"));

        if (player instanceof HumanPlayer) {
            ((HumanPlayer) player).getInventory().addCard(card);
        }

        if (card instanceof VictoryPointCard) {
            int vp = victoryManager.computeVictoryPoints(player, state.getBoard());
            if (vp >= victoryManager.getVictoryThreshold()) {
                state.setWinner(player);
                notifyListeners(l -> l.onGameWon(player));
                return card;
            }
        }

        cardBoughtThisTurn = card;
        notifyListeners(l -> l.onCardBought(player, card));
        return card;
    }

    public void playCard(ExperimentCard card) {
        assert card != null : "Card to play must not be null";
        assert state.getCurrentPhase() == TurnPhase.TRADE_BUILD
                || state.getCurrentPhase() == TurnPhase.ROLL_DICE
                : "playCard() called in invalid phase: " + state.getCurrentPhase();
        assert !state.isGameOver() : "playCard() called after game is over";
        assert !cardPlayedThisTurn
                : "Only one card can be played per turn";

        Player player = state.getActivePlayer();
        if (player instanceof HumanPlayer) {
            assert ((HumanPlayer) player).getInventory().getCards().contains(card)
                    : "Player " + player.getName() + " does not hold card: " + card.getCardName();
        }
        assert !(card == cardBoughtThisTurn && !(card instanceof VictoryPointCard))
                : "Cannot play a card bought in the same turn (except VictoryPointCard on winning condition)";

        if (player instanceof HumanPlayer) {
            ((HumanPlayer) player).getInventory().removeCard(card);
        }
        cardPlayedThisTurn = true;

        if (card instanceof KnightCard) {
            if (player instanceof HumanPlayer) {
                ((HumanPlayer) player).getInventory().incrementKnightCardsPlayed();
            }
            roadManager.updateLargestArmy(state, player);
            notifyListeners(l -> l.onKnightPlayed(player));
            state.setCurrentPhase(TurnPhase.ROBBER_MOVE);
        } else if (card instanceof RoadBuildingCard) {
            freeRoadsRemaining = 2;
            notifyListeners(l -> l.onCardPlayed(player, card));
        } else if (card instanceof MonopolyCard) {
            notifyListeners(l -> l.onCardPlayed(player, card));
        } else {
            card.applyEffect(state, player);
            notifyListeners(l -> l.onCardPlayed(player, card));
        }

        checkVictoryCondition();
    }

    public void applyMonopolyCard(MonopolyCard card, ResourceType chosenResource) {
        assert card != null : "Card must not be null";
        assert chosenResource != null : "Resource must not be null";

        card.setChosenResource(chosenResource);
        card.applyEffect(state, state.getActivePlayer());
        notifyListeners(l -> l.onCardPlayed(state.getActivePlayer(), card));
    }

    public void moveRobber(HexCoordinate newPosition) {
        assert state.getCurrentPhase() == TurnPhase.ROBBER_MOVE
                || state.getCurrentPhase() == TurnPhase.ROBBER_DISCARD
                : "moveRobber() called in invalid phase: " + state.getCurrentPhase();

        robberManager.moveRobber(state, newPosition);
        notifyListeners(l -> l.onRobberMoved(newPosition));

        if (!diceRolledThisTurn) {
            state.setCurrentPhase(TurnPhase.ROLL_DICE);
        } else {
            state.setCurrentPhase(TurnPhase.TRADE_BUILD);
            if (!state.isSetupPhase()) {
                startTradeBuildTimer();
            }
        }
        notifyListeners(l -> l.onPhaseChanged(state.getCurrentPhase()));
    }

    public void discardResources(Player player, Map<ResourceType, Integer> toDiscard) {
        assert player != null : "Player must not be null";
        assert toDiscard != null : "Discard map must not be null";

        robberManager.discardResources(player, toDiscard, state.getBank());
        notifyListeners(l -> l.onResourcesDiscarded(player));

        List<Player> stillNeedToDiscard = robberManager.getPlayersRequiredToDiscard(
                state.getAllPlayers());

        if (stillNeedToDiscard.isEmpty()) {
            state.setCurrentPhase(TurnPhase.ROBBER_MOVE);
            notifyListeners(l -> l.onPhaseChanged(state.getCurrentPhase()));
        }
    }

    public Optional<ResourceType> stealFromPlayer(Player targetPlayer) {
        assert targetPlayer != null : "Target player must not be null";

        Player activePlayer = state.getActivePlayer();
        if (targetPlayer.getTotalResourceCount() == 0) {
            return Optional.empty();
        }

        ResourceType stolen = robberManager.stealRandomResource(
                activePlayer, targetPlayer, state.getBank());
        notifyListeners(l -> l.onResourceStolen(activePlayer, targetPlayer, stolen));
        return Optional.of(stolen);
    }

    public List<Player> getStealCandidates() {
        return robberManager.getStealCandidates(
                state.getBoard(),
                state.getAllPlayers(),
                state.getActivePlayer(),
                state.getRobberPosition()
        );
    }

    public List<Player> getPlayersRequiredToDiscard() {
        return robberManager.getPlayersRequiredToDiscard(state.getAllPlayers());
    }

    public int computeDiscardCount(Player player) {
        return robberManager.computeDiscardCount(player);
    }

    public void endTurn() {
        assert state.getCurrentPhase() == TurnPhase.TRADE_BUILD
                : "endTurn() called outside TRADE_BUILD phase, current phase: "
                  + state.getCurrentPhase();
        assert !state.isGameOver() : "endTurn() called after game is over";

        tradeBuildPhaseHandler.handle(state, this);
        cardPlayedThisTurn = false;
        cardBoughtThisTurn = null;
        freeRoadsRemaining = 0;
        diceRolledThisTurn = false;

        notifyListeners(l -> l.onTurnChanged(state.getActivePlayer()));
        notifyListeners(l -> l.onPhaseChanged(state.getCurrentPhase()));
    }

    public DiceResult getLastDiceResult() {
        return lastDiceResult;
    }

    public int getFreeRoadsRemaining() {
        return freeRoadsRemaining;
    }

    public boolean isCardPlayedThisTurn() {
        return cardPlayedThisTurn;
    }

    public void distributeResources(int diceTotal) {
        resourceDistributor.distribute(
                state.getBoard(),
                state.getAllPlayers(),
                state.getBank(),
                diceTotal,
                state.getRobberPosition()
        );
        notifyListeners(l -> l.onResourcesDistributed(state.getActivePlayer()));
    }

    public void startTradeBuildTimer() {
        turnTimer.start();
        notifyListeners(l -> l.onTimerStarted(TurnTimer.TURN_DURATION_SECONDS));
    }

    public void stopTradeBuildTimer() {
        turnTimer.stop();
    }

    public void pauseTimer() {
        turnTimer.pause();
    }

    public void resumeTimer() {
        turnTimer.resume();
    }

    public int getTimerSecondsRemaining() {
        return turnTimer.getSecondsRemaining();
    }

    public void shutdown() {
        turnTimer.shutdown();
    }

    private void onTurnTimerExpired() {
        if (state.getCurrentPhase() != TurnPhase.TRADE_BUILD) return;
        notifyListeners(l -> l.onTimerExpired(state.getActivePlayer()));
        tradeBuildPhaseHandler.handle(state, this);
        cardPlayedThisTurn = false;
        cardBoughtThisTurn = null;
        freeRoadsRemaining = 0;
        diceRolledThisTurn = false;
        notifyListeners(l -> l.onTurnChanged(state.getActivePlayer()));
        notifyListeners(l -> l.onPhaseChanged(state.getCurrentPhase()));
    }

    private void onTurnTimerTick(int secondsRemaining) {
        notifyListeners(l -> l.onTimerTick(secondsRemaining));
    }

    private void checkVictoryCondition() {
        if (state.isGameOver()) return;
        if (state.getCurrentPhase() != TurnPhase.TRADE_BUILD) return;

        victoryManager.checkWinner(state).ifPresent(winner -> {
            state.setWinner(winner);
            turnTimer.stop();
            notifyListeners(l -> l.onGameWon(winner));
        });
    }

    private void payBuildCost(Player player, model.building.BuildCost cost) {
        if (!player.canAfford(cost.getAllCosts())) {
            throw new InvalidBuildException(
                    "Player " + player.getName() + " cannot afford build cost: " + cost);
        }
        for (Map.Entry<ResourceType, Integer> entry : cost.getAllCosts().entrySet()) {
            player.removeResource(entry.getKey(), entry.getValue());
            state.getBank().returnResource(entry.getKey(), entry.getValue());
        }
    }

    private void notifyListeners(Consumer<GameEngineListener> action) {
        for (GameEngineListener listener : listeners) {
            action.accept(listener);
        }
    }
}
