package model.building;

/**
 * 1 VP, yields 1 resource per production
 * max 5 per player, harus diplace sblm upgrade to Laboratory.
 */
public final class Settlement implements Building {

    private final String ownerName;

    public Settlement(String ownerName) {
        assert ownerName != null && !ownerName.isBlank() : "Settlement owner name must not be blank";
        this.ownerName = ownerName;
    }

    @Override public String getOwnerName() { return ownerName; }
    @Override public int getVictoryPoints() { return 1; }
    @Override public int getResourceYield() { return 1; }
    @Override public String getBuildingTypeName(){ return "Pos Pantau"; }

    @Override
    public String toString() {
        return "Settlement{owner=" + ownerName + "}";
    }
}
