package model.building;

/*
 * 2 VP, 2 resources per production
 * harus di built by upgrading an existing settlement
 * max 4 per player
 */
public final class Laboratory implements Building {

    private final String ownerName;

    public Laboratory(String ownerName) {
        assert ownerName != null && !ownerName.isBlank() : "Laboratory owner name must not be blank";
        this.ownerName = ownerName;
    }

    @Override public String getOwnerName() { return ownerName; }
    @Override public int getVictoryPoints() { return 2; }
    @Override public int getResourceYield() { return 2; }
    @Override public String getBuildingTypeName(){ return "Laboratorium"; }

    @Override
    public String toString() {
        return "Laboratory{owner=" + ownerName + "}";
    }
}
