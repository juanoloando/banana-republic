package model.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


// represents posisi hex tile di board, pake axial coordinate system (q,r) 
public final class HexCoordinate {

    private final int q;
    private final int r;
    private final int s;

    public HexCoordinate(int q, int r) {
        this.q = q;
        this.r = r;
        this.s = -q - r;
        assert q + r + s == 0 : "Cube coordinate invariant violated: q+r+s must equal 0";
    }

    public int getQ() {return q; }
    public int getR() {return r; }
    public int getS() {return s; }

    public List<HexCoordinate> getNeighbors() {
        int[][] directions = {{1,0},{1,-1},{0,-1},{-1,0},{-1,1},{0,1}};
        List<HexCoordinate> neighbors = new ArrayList<>();
        for (int[] d : directions) {
            neighbors.add(new HexCoordinate(q + d[0], r + d[1]));
        }
        return neighbors;
    }

    // manhattan distance in cube coordinates
    public int distanceTo(HexCoordinate other) {
        return (Math.abs(q - other.q) + Math.abs(r - other.r) + Math.abs(s - other.s)) / 2;
    }

    public boolean isAdjacentTo(HexCoordinate other) {
        return distanceTo(other) == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HexCoordinate)) return false;
        HexCoordinate that = (HexCoordinate) o;
        return q == that.q && r == that.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }

    @Override
    public String toString() {
        return "HexCoordinate{q=" + q + ", r=" + r + ", s=" + s + "}";
    }
}
