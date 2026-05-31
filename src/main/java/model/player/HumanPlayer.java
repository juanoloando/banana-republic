package model.player;

import model.exception.BuildLimitExceededException;
import model.resource.ResourceType;

import java.util.*;

public class HumanPlayer implements Player {

    public static final int MAX_SETTLEMENTS  = 5;
    public static final int MAX_LABORATORIES = 4;
    public static final int MAX_PIPES        = 15;

    private final String name;
    private final PlayerColor color;

    private final PlayerInventory inventory;

    private int settlementsInSupply;
    private int laboratoriesInSupply;
    private int pipesInSupply;

    private final Set<Integer> occupiedIntersectionIds;
    private final Set<Integer> occupiedPathIds;

    private final Set<SpecialCard> heldSpecialCards;

    public HumanPlayer(String name, PlayerColor color) {
        assert name != null && !name.isBlank() : "Player name must not be blank";
        assert color != null : "Player color must not be null";
        this.name = name;
        this.color = color;
        this.inventory = new PlayerInventory();
        this.settlementsInSupply  = MAX_SETTLEMENTS;
        this.laboratoriesInSupply = MAX_LABORATORIES;
        this.pipesInSupply        = MAX_PIPES;
        this.occupiedIntersectionIds = new HashSet<>();
        this.occupiedPathIds         = new HashSet<>();
        this.heldSpecialCards        = EnumSet.noneOf(SpecialCard.class);
    }

    @Override public String getName()                        { return name; }
    @Override public PlayerColor getColor()                  { return color; }
    @Override public int getResourceCount(ResourceType type) { return inventory.getResourceCount(type); }
    @Override public int getTotalResourceCount()             { return inventory.getTotalResourceCount(); }
    @Override public void addResource(ResourceType type, int amount) { inventory.addResource(type, amount); }
    @Override public void removeResource(ResourceType type, int amount) { inventory.removeResource(type, amount); }

    @Override
    public boolean canAfford(Map<? extends ResourceType, ? extends Integer> cost) {
        return inventory.canAfford(cost);
    }

    public PlayerInventory getInventory() { return inventory; }

    public void placeSettlement(int intersectionId) {
        assert intersectionId >= 0 : "intersectionId must be non-negative, got: " + intersectionId;
        assert !occupiedIntersectionIds.contains(intersectionId)
            : "Player " + name + " already has a building at intersection " + intersectionId;
        if (settlementsInSupply <= 0) {
            throw new BuildLimitExceededException("Settlement", MAX_SETTLEMENTS);
        }
        settlementsInSupply--;
        occupiedIntersectionIds.add(intersectionId);
    }

    public void upgradeLaboratory(int intersectionId) {
        assert intersectionId >= 0 : "intersectionId must be non-negative, got: " + intersectionId;
        assert occupiedIntersectionIds.contains(intersectionId)
                : "Player " + name + " has no building at intersection " + intersectionId;
        if (laboratoriesInSupply <= 0) {
            throw new BuildLimitExceededException("Laboratory", MAX_LABORATORIES);
        }
        laboratoriesInSupply--;
        settlementsInSupply++;  // settlement piece dikembalikan ke stok 
    }

    public void placePipe(int pathId) {
        assert pathId >= 0 : "pathId must be non-negative, got: " + pathId;
        assert !occupiedPathIds.contains(pathId)
            : "Player " + name + " already has a pipe at path " + pathId;
        if (pipesInSupply <= 0) {
            throw new BuildLimitExceededException("Pipe", MAX_PIPES);
        }
        pipesInSupply--;
        occupiedPathIds.add(pathId);
    }


    public int getSettlementsInSupply()  { return settlementsInSupply; }
    public int getLaboratoriesInSupply() { return laboratoriesInSupply; }
    public int getPipesInSupply()        { return pipesInSupply; }


    public Set<Integer> getOccupiedIntersectionIds() {
        return Collections.unmodifiableSet(occupiedIntersectionIds);
    }

    public Set<Integer> getOccupiedPathIds() {
        return Collections.unmodifiableSet(occupiedPathIds);
    }

    public boolean hasBuiltAt(int intersectionId) {
        return occupiedIntersectionIds.contains(intersectionId);
    }

    public boolean hasPipeAt(int pathId) {
        return occupiedPathIds.contains(pathId);
    }

    public void grantSpecialCard(SpecialCard card) {
        assert card != null : "SpecialCard to grant must not be null";
        assert !heldSpecialCards.contains(card)
            : "Player " + name + " already holds " + card.getDisplayName();
        heldSpecialCards.add(card);
    }

    public void revokeSpecialCard(SpecialCard card) {
        assert card != null : "SpecialCard to revoke must not be null";
        assert heldSpecialCards.contains(card)
            : "Player " + name + " does not hold " + card.getDisplayName()
              + " — cannot revoke";
        heldSpecialCards.remove(card);
    }

    public boolean holdsSpecialCard(SpecialCard card) {
        return heldSpecialCards.contains(card);
    }

    public Set<SpecialCard> getHeldSpecialCards() {
        return Collections.unmodifiableSet(heldSpecialCards);
    }

    public int getSpecialCardVictoryPoints() {
        return heldSpecialCards.stream()
                .mapToInt(SpecialCard::getVictoryPoints)
                .sum();
    }

    @Override
    public String toString() {
        return "HumanPlayer{name='" + name + "', color=" + color + "}";
    }
}
