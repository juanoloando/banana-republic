package engine;

import model.board.Board;
import model.board.HexCoordinate;
import model.card.CardDeck;
import model.card.GameStateContext;
import model.player.Player;
import model.player.SpecialCard;
import model.resource.Bank;
import model.resource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameStateImpl implements GameState, GameStateContext {

    private final Board board;
    private final List<Player> players;
    private final Bank bank;
    private final CardDeck deck;

    private int currentPlayerIndex;
    private TurnPhase currentPhase;
    private HexCoordinate robberPosition;
    private int roundNumber;
    private boolean gameOver;
    private Player winner;

    private final Map<SpecialCard, Player> specialCardHolders;

    private int setupTurnIndex;
    private boolean setupSecondRoundStarted;
    private int longestRoadLength;
    private int largestArmySize;

    public GameStateImpl(Board board, List<Player> players, Bank bank, CardDeck deck,
                         HexCoordinate initialRobberPosition) {
        assert board != null : "Board must not be null";
        assert players != null && players.size() >= 3 && players.size() <= 4
                : "Game requires 3-4 players, got: " + (players == null ? "null" : players.size());
        assert bank != null : "Bank must not be null";
        assert deck != null : "Deck must not be null";
        assert initialRobberPosition != null : "Initial robber position must not be null";

        this.board = board;
        this.players = Collections.unmodifiableList(players);
        this.bank = bank;
        this.deck = deck;
        this.robberPosition = initialRobberPosition;
        this.currentPlayerIndex = 0;
        this.currentPhase = TurnPhase.SETUP_PLACE_FIRST;
        this.roundNumber = 1;
        this.gameOver = false;
        this.winner = null;
        this.specialCardHolders = new EnumMap<>(SpecialCard.class);
        this.setupTurnIndex = 0;
        this.setupSecondRoundStarted = false;
        this.longestRoadLength = 0;
        this.largestArmySize = 0;
    }

    @Override
    public List<Player> getAllPlayers() {
        return players;
    }

    @Override
    public Bank getBank() {
        return bank;
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public CardDeck getDeck() {
        return deck;
    }

    @Override
    public Player getActivePlayer() {
        return players.get(currentPlayerIndex);
    }

    @Override
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    @Override
    public HexCoordinate getRobberPosition() {
        return robberPosition;
    }

    @Override
    public Optional<Player> getSpecialCardHolder(SpecialCard card) {
        return Optional.ofNullable(specialCardHolders.get(card));
    }

    @Override
    public TurnPhase getCurrentPhase() {
        return currentPhase;
    }

    @Override
    public int getRoundNumber() {
        return roundNumber;
    }

    @Override
    public boolean isSetupPhase() {
        return currentPhase.isSetup();
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }

    public void setCurrentPhase(TurnPhase phase) {
        assert phase != null : "Phase must not be null";
        this.currentPhase = phase;
    }

    public void setCurrentPlayerIndex(int index) {
        assert index >= 0 && index < players.size()
                : "Player index out of bounds: " + index;
        this.currentPlayerIndex = index;
    }

    public void setRobberPosition(HexCoordinate position) {
        assert position != null : "Robber position must not be null";
        this.robberPosition = position;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void setWinner(Player winner) {
        assert winner != null : "Winner must not be null";
        this.winner = winner;
        this.gameOver = true;
        this.currentPhase = TurnPhase.GAME_OVER;
    }

    public void incrementRoundNumber() {
        this.roundNumber++;
    }

    public void grantSpecialCard(SpecialCard card, Player newHolder) {
        assert card != null : "SpecialCard must not be null";
        assert newHolder != null : "New holder must not be null";

        Player currentHolder = specialCardHolders.get(card);
        if (currentHolder != null && !currentHolder.getName().equals(newHolder.getName())) {
            if (currentHolder instanceof model.player.HumanPlayer) {
                ((model.player.HumanPlayer) currentHolder).revokeSpecialCard(card);
            }
        }
        specialCardHolders.put(card, newHolder);
        if (newHolder instanceof model.player.HumanPlayer) {
            model.player.HumanPlayer hp = (model.player.HumanPlayer) newHolder;
            if (!hp.holdsSpecialCard(card)) {
                hp.grantSpecialCard(card);
            }
        }
    }

    public void revokeSpecialCard(SpecialCard card) {
        assert card != null : "SpecialCard must not be null";
        Player holder = specialCardHolders.remove(card);
        if (holder instanceof model.player.HumanPlayer) {
            model.player.HumanPlayer hp = (model.player.HumanPlayer) holder;
            if (hp.holdsSpecialCard(card)) {
                hp.revokeSpecialCard(card);
            }
        }
    }

    public int getLongestRoadLength() {
        return longestRoadLength;
    }

    public void setLongestRoadLength(int length) {
        assert length >= 0 : "Road length must be non-negative";
        this.longestRoadLength = length;
    }

    public int getLargestArmySize() {
        return largestArmySize;
    }

    public void setLargestArmySize(int size) {
        assert size >= 0 : "Army size must be non-negative";
        this.largestArmySize = size;
    }

    public int getSetupTurnIndex() {
        return setupTurnIndex;
    }

    public void setSetupTurnIndex(int index) {
        this.setupTurnIndex = index;
    }

    public boolean isSetupSecondRoundStarted() {
        return setupSecondRoundStarted;
    }

    public void setSetupSecondRoundStarted(boolean started) {
        this.setupSecondRoundStarted = started;
    }

    @Override
    public void moveRobber(int tileIndex) {
        board.getAllTiles().stream()
                .filter(t -> !t.getCoordinate().equals(robberPosition))
                .skip(tileIndex)
                .findFirst()
                .ifPresent(t -> this.robberPosition = t.getCoordinate());
    }

    @Override
    public void giveResourceToPlayer(Player player, ResourceType type, int amount) {
        assert player != null : "Player must not be null";
        assert type != null : "ResourceType must not be null";
        assert amount > 0 : "Amount must be positive";

        if (bank.hasResource(type, amount)) {
            bank.takeResource(type, amount);
            player.addResource(type, amount);
        }
    }
}
