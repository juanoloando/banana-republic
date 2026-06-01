package model.board;

import model.building.Building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// vertex dmn 3 hex tile ketemu, tempat bangun settlement/lab
public class Intersection {

    private final int id;
    private final List<HexCoordinate> adjacentTileCoords;
    private final List<Integer> adjacentIntersectionIds;
    private final List<Integer> adjacentPathIds;
    private Building building;
    private Harbor harbor;

    public Intersection(int id, List<HexCoordinate> adjacentTileCoords,
                        List<Integer> adjacentIntersectionIds, List<Integer> adjacentPathIds) {
        this.id = id;
        this.adjacentTileCoords = new ArrayList<>(adjacentTileCoords);
        this.adjacentIntersectionIds = new ArrayList<>(adjacentIntersectionIds);
        this.adjacentPathIds = new ArrayList<>(adjacentPathIds);
        this.building = null;
        this.harbor = null;
    }

    public int getId() { return id; }

    public List<HexCoordinate> getAdjacentTileCoords() {
        return Collections.unmodifiableList(adjacentTileCoords);
    }

    public List<Integer> getAdjacentIntersectionIds() {
        return Collections.unmodifiableList(adjacentIntersectionIds);
    }

    public List<Integer> getAdjacentPathIds() {
        return Collections.unmodifiableList(adjacentPathIds);
    }

    public Optional<Building> getBuilding() {
        return Optional.ofNullable(building);
    }

    public Optional<Harbor> getHarbor() {
        return Optional.ofNullable(harbor);
    }

    public void placeBuilding(Building building) {
        assert building != null : "Cannot place a null building";
        assert this.building == null : "Intersection " + id + " already has a building";
        this.building = building;
    }

    public void upgradeBuilding(Building newBuilding) {
        assert newBuilding != null : "Cannot upgrade to a null building";
        assert this.building != null : "No building to upgrade at intersection " + id;
        this.building = newBuilding;
    }

    public void setHarbor(Harbor harbor) {
        this.harbor = harbor;
    }

    public boolean isEmpty() {
        return building == null;
    }

    public boolean hasHarbor() {
        return harbor != null;
    }

    public boolean isOccupiedBy(String playerName) {
        return building != null && building.getOwnerName().equals(playerName);
    }

    // Package-private: used by IntersectionPathBuilder to populate adjacency after construction
    void addAdjacentIntersectionId(int id) {
        if (!adjacentIntersectionIds.contains(id)) {
            adjacentIntersectionIds.add(id);
        }
    }

    void addAdjacentPathId(int id) {
        if (!adjacentPathIds.contains(id)) {
            adjacentPathIds.add(id);
        }
    }

    @Override
    public String toString() {
        return "Intersection{id=" + id
                + ", building=" + (building != null ? building.getClass().getSimpleName() : "none")
                + ", harbor=" + (harbor != null ? harbor.getType() : "none") + "}";
    }
}
