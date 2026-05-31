package model.board;

import model.resource.ResourceType;

import java.util.Optional;

/**
 * Represents a single hexagonal tile on the board.
 * Immutable after construction — tiles don't change during a game,
 * only the Robber position (tracked separately in GameState) changes.
 *
 * Token number rules from spec:
 *  - Token 7 does not exist
 *  - Tokens 2 and 12: exactly 1 each
 *  - Tokens 3-6 and 8-11: exactly 2 each
 *  - DESERT has no token (represented as 0)
 */
public final class HexTile {

    private final HexCoordinate coordinate;
    private final TerrainType terrainType;
    private final int tokenNumber;

    public HexTile(HexCoordinate coordinate, TerrainType terrainType, int tokenNumber) {
        validateTokenNumber(terrainType, tokenNumber);
        this.coordinate = coordinate;
        this.terrainType = terrainType;
        this.tokenNumber = tokenNumber;
    }

    private void validateTokenNumber(TerrainType terrain, int token) {
        if (terrain == TerrainType.DESERT) {
            if (token != 0) {
                throw new model.exception.InvalidBoardException(
                    "Desert tile must have token 0, got: " + token);
            }
            return;
        }
        if (token == 7) {
            throw new model.exception.InvalidBoardException(
                "Token 7 is not allowed per game rules");
        }
        if (token < 2 || token > 12) {
            throw new model.exception.InvalidBoardException(
                "Token must be between 2-12, got: " + token);
        }
    }

    public HexCoordinate getCoordinate() { return coordinate; }
    public TerrainType getTerrainType() { return terrainType; }
    public int getTokenNumber() { return tokenNumber; }

    public boolean isProductive() {
        return terrainType.isProductive();
    }

    public Optional<ResourceType> getProducedResource() {
        return terrainType.getProducedResource();
    }

    public boolean isHighProbability() {
        return tokenNumber == 6 || tokenNumber == 8;
    }

    @Override
    public String toString() {
        return "HexTile{coord=" + coordinate + ", terrain=" + terrainType
                + ", token=" + tokenNumber + "}";
    }
}
