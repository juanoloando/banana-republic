package plugin;

import java.util.Map;

import engine.DiceResult;
import model.board.HexCoordinate;
import model.player.Player;
import model.resource.ResourceType;

// dipublish tiap kali lempar dadu
class DiceRolledEvent extends GameEvent {

    private final DiceResult result;
    private final Player rollingPlayer;

    public DiceRolledEvent(DiceResult result, Player rollingPlayer) {
        super();
        assert result != null : "DiceResult tidak boleh null";
        assert rollingPlayer != null : "Rolling player tidak boleh null";
        this.result = result;
        this.rollingPlayer = rollingPlayer;
    }

    public DiceResult getResult() {
        return result;
    }

    public Player getRollingPlayer() {
        return rollingPlayer;
    }

    @Override
    public String getEventName() {
        return "DiceRolled";
    }
}

//dipublish ketika pemain menempatkan bangunan (Settlement, Lab, atau Pipa)
class BuildingPlacedEvent extends GameEvent {

    private final Player player;
    private final String buildingType;
    private final int locationId;

    public BuildingPlacedEvent(Player player, String buildingType, int locationId) {
        super();
        assert player != null : "Player tidak boleh null";
        assert buildingType != null && !buildingType.isBlank() : "Building type tidak boleh blank";
        this.player = player;
        this.buildingType = buildingType;
        this.locationId = locationId;
    }

    public Player getPlayer() {
        return player;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public int getLocationId() {
        return locationId;
    }

    @Override
    public String getEventName() {
        return "BuildingPlaced";
    }
}

// dipublish ketika pergantian giliran pemain
class TurnChangedEvent extends GameEvent {

    private final Player previousPlayer;
    private final Player newActivePlayer;
    private final int roundNumber;

    public TurnChangedEvent(Player previousPlayer, Player newActivePlayer, int roundNumber) {
        super();
        assert newActivePlayer != null : "New active player tidak boleh null";
        this.previousPlayer = previousPlayer;
        this.newActivePlayer = newActivePlayer;
        this.roundNumber = roundNumber;
    }

    public Player getPreviousPlayer() {
        return previousPlayer;
    }

    public Player getNewActivePlayer() {
        return newActivePlayer;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    @Override
    public String getEventName() {
        return "TurnChanged";
    }
}

// dipublish ketika pencuri dipindahkan
class RobberMovedEvent extends GameEvent {

    private final Player movingPlayer;
    private final HexCoordinate fromPosition;
    private final HexCoordinate toPosition;
    private final boolean fromKnightCard;

    public RobberMovedEvent(Player movingPlayer, HexCoordinate fromPosition, HexCoordinate toPosition, boolean fromKnightCard) {
        super();
        assert movingPlayer != null : "Moving player tidak boleh null";
        assert toPosition != null : "Destination tidak boleh null";
        this.movingPlayer = movingPlayer;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.fromKnightCard = fromKnightCard;
    }

    public Player getMovingPlayer() {
        return movingPlayer;
    }

    public HexCoordinate getFromPosition() {
        return fromPosition;
    }

    public HexCoordinate getToPosition() {
        return toPosition;
    }

    public boolean isFromKnightCard() {
        return fromKnightCard;
    }

    @Override
    public String getEventName() {
        return "RobberMoved";
    }
}

// dipublish ketika terjadi perdagangan 
class TradeExecutedEvent extends GameEvent {

    public enum TradeType { DOMESTIC, MARITIME }

    private final Player initiator;
    private final Player counterpart; 
    private final Map<ResourceType, Integer> offered;
    private final Map<ResourceType, Integer> received;
    private final TradeType tradeType;

    public TradeExecutedEvent(Player initiator, Player counterpart, Map<ResourceType, Integer> offered, Map<ResourceType, Integer> received, TradeType tradeType) {
        super();
        assert initiator != null : "Trade initiator tidak boleh null";
        assert offered != null : "Offered resources tidak boleh null";
        assert received != null : "Received resources tidak boleh null";
        this.initiator = initiator;
        this.counterpart = counterpart;
        this.offered = Map.copyOf(offered);
        this.received = Map.copyOf(received);
        this.tradeType = tradeType;
    }

    public Player getInitiator() {
        return initiator;
    }

    public Player getCounterpart() {
        return counterpart;
    }

    public Map<ResourceType, Integer> getOffered() {
        return offered;
    }

    public Map<ResourceType, Integer> getReceived() {
        return received;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    @Override
    public String getEventName() {
        return "TradeExecuted";
    }
}

// dipublish ketika seorang pemain memenangkan permainan
class GameWonEvent extends GameEvent {

    private final Player winner;
    private final int finalVictoryPoints;
    private final int roundNumber;

    public GameWonEvent(Player winner, int finalVictoryPoints, int roundNumber) {
        super();
        assert winner != null : "Winner tidak boleh null";
        assert finalVictoryPoints >= 10 : "Winner harus memiliki setidaknya 10 VP, didapat: " + finalVictoryPoints;
        this.winner = winner;
        this.finalVictoryPoints = finalVictoryPoints;
        this.roundNumber = roundNumber;
    }

    public Player getWinner() {
        return winner;
    }

    public int getFinalVictoryPoints() {
        return finalVictoryPoints;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    @Override
    public String getEventName() {
        return "GameWon";
    }
}
