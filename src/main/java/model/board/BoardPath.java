package model.board;

import model.building.Pipe;
import java.util.Optional;


public class BoardPath {

    private final int id;
    private final int intersectionIdA;
    private final int intersectionIdB;
    private Pipe pipe;

    public BoardPath(int id, int intersectionIdA, int intersectionIdB) {
        assert intersectionIdA != intersectionIdB : "A path cannot connect an intersection to itself";
        this.id = id;
        this.intersectionIdA = intersectionIdA;
        this.intersectionIdB = intersectionIdB;
        this.pipe = null;
    }

    public int getId() { return id; }
    public int getIntersectionIdA() { return intersectionIdA; }
    public int getIntersectionIdB() { return intersectionIdB; }

    public Optional<Pipe> getPipe() {
        return Optional.ofNullable(pipe);
    }

    public void placePipe(Pipe pipe) {
        assert pipe != null : "Cannot place a null pipe";
        assert this.pipe == null : "Path " + id + " already has a pipe";
        this.pipe = pipe;
    }

    public boolean isEmpty() {
        return pipe == null;
    }

    public boolean isOwnedBy(String playerName) {
        return pipe != null && pipe.getOwnerName().equals(playerName);
    }

    public boolean connectsTo(int intersectionId) {
        return intersectionIdA == intersectionId || intersectionIdB == intersectionId;
    }

    public int getOtherIntersection(int intersectionId) {
        assert connectsTo(intersectionId)
                : "Intersection " + intersectionId + " is not an endpoint of path " + id;
        return intersectionIdA == intersectionId ? intersectionIdB : intersectionIdA;
    }

    @Override
    public String toString() {
        return "BoardPath{id=" + id + ", [" + intersectionIdA + "<->" + intersectionIdB + "]"
                + ", pipe=" + (pipe != null ? pipe.getOwnerName() : "none") + "}";
    }
}
