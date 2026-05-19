package model.building;


// konektivitas jaringan dan penghitungan jalan terpanjang, maks 15 per pemain
public final class Pipe {

    private final String ownerName;

    public Pipe(String ownerName) {
        assert ownerName != null && !ownerName.isBlank(): "Pipe owner name must not be blank";
        this.ownerName = ownerName;
    }

    public String getOwnerName() { return ownerName; }
    public String getBuildingTypeName() { return "Pipa Transportasi"; }

    @Override
    public String toString() {
        return "Pipe{owner=" + ownerName + "}";
    }
}
