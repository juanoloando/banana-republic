package engine;

import model.board.Board;
import model.board.BoardPath;
import model.board.Intersection;
import model.exception.InvalidBuildException;
import model.player.HumanPlayer;
import model.player.Player;

import java.util.List;

public class BuildValidator {

    public void validateSettlementPlacement(Board board, Player player, int intersectionId,
                                            boolean isSetupPhase) {
        assert board != null : "Board must not be null";
        assert player != null : "Player must not be null";
        assert intersectionId >= 0 : "IntersectionId must be non-negative";

        Intersection intersection = board.getIntersection(intersectionId)
                .orElseThrow(() -> new InvalidBuildException(
                        "Intersection " + intersectionId + " does not exist"));

        if (!intersection.isEmpty()) {
            throw new InvalidBuildException(
                    "Intersection " + intersectionId + " is already occupied");
        }

        checkDistanceRule(board, intersectionId);

        if (!isSetupPhase) {
            checkSettlementConnectivity(board, player, intersectionId);
        }

        if (player instanceof HumanPlayer) {
            HumanPlayer hp = (HumanPlayer) player;
            if (hp.getSettlementsInSupply() <= 0) {
                throw new model.exception.BuildLimitExceededException(
                        "Settlement", HumanPlayer.MAX_SETTLEMENTS);
            }
        }
    }

    public void validateLaboratoryUpgrade(Board board, Player player, int intersectionId) {
        assert board != null : "Board must not be null";
        assert player != null : "Player must not be null";
        assert intersectionId >= 0 : "IntersectionId must be non-negative";

        Intersection intersection = board.getIntersection(intersectionId)
                .orElseThrow(() -> new InvalidBuildException(
                        "Intersection " + intersectionId + " does not exist"));

        if (intersection.isEmpty()) {
            throw new InvalidBuildException(
                    "No building at intersection " + intersectionId + " to upgrade");
        }

        intersection.getBuilding().ifPresent(b -> {
            if (!b.getOwnerName().equals(player.getName())) {
                throw new InvalidBuildException(
                        "Building at intersection " + intersectionId
                                + " does not belong to " + player.getName());
            }
            if (b instanceof model.building.Laboratory) {
                throw new InvalidBuildException(
                        "Building at intersection " + intersectionId
                                + " is already a Laboratory");
            }
        });

        if (player instanceof HumanPlayer) {
            HumanPlayer hp = (HumanPlayer) player;
            if (hp.getLaboratoriesInSupply() <= 0) {
                throw new model.exception.BuildLimitExceededException(
                        "Laboratory", HumanPlayer.MAX_LABORATORIES);
            }
        }
    }

    public void validatePipePlacement(Board board, Player player, int pathId,
                                      boolean isSetupPhase, Integer setupIntersectionId) {
        assert board != null : "Board must not be null";
        assert player != null : "Player must not be null";
        assert pathId >= 0 : "PathId must be non-negative";

        BoardPath path = board.getPath(pathId)
                .orElseThrow(() -> new InvalidBuildException(
                        "Path " + pathId + " does not exist"));

        if (!path.isEmpty()) {
            throw new InvalidBuildException("Path " + pathId + " already has a pipe");
        }

        if (player instanceof HumanPlayer) {
            HumanPlayer hp = (HumanPlayer) player;
            if (hp.getPipesInSupply() <= 0) {
                throw new model.exception.BuildLimitExceededException(
                        "Pipe", HumanPlayer.MAX_PIPES);
            }
        }

        if (isSetupPhase && setupIntersectionId != null) {
            boolean touchesSetupSettlement =
                    path.connectsTo(setupIntersectionId);
            if (!touchesSetupSettlement) {
                throw new InvalidBuildException(
                        "During setup, pipe must be adjacent to the settlement just placed "
                                + "(intersection " + setupIntersectionId + ")");
            }
        } else {
            checkPipeConnectivity(board, player, pathId);
        }
    }

    private void checkDistanceRule(Board board, int intersectionId) {
        List<Intersection> neighbors = board.getNeighborIntersections(intersectionId);
        for (Intersection neighbor : neighbors) {
            if (!neighbor.isEmpty()) {
                throw new InvalidBuildException(
                        "Distance rule violated: intersection " + neighbor.getId()
                                + " adjacent to " + intersectionId + " is already occupied");
            }
        }
    }

    private void checkSettlementConnectivity(Board board, Player player, int intersectionId) {
        List<BoardPath> paths = board.getPathsAt(intersectionId);
        boolean connected = paths.stream()
                .anyMatch(p -> p.isOwnedBy(player.getName()));
        if (!connected) {
            throw new InvalidBuildException(
                    "Settlement must be connected to at least one of your Pipes "
                            + "(intersection " + intersectionId + ")");
        }
    }

    private void checkPipeConnectivity(Board board, Player player, int pathId) {
        BoardPath path = board.getPath(pathId)
                .orElseThrow(() -> new InvalidBuildException("Path not found: " + pathId));

        int idA = path.getIntersectionIdA();
        int idB = path.getIntersectionIdB();

        boolean connectedAtA = isConnectedToPlayerNetwork(board, player, idA);
        boolean connectedAtB = isConnectedToPlayerNetwork(board, player, idB);

        if (!connectedAtA && !connectedAtB) {
            throw new InvalidBuildException(
                    "Pipe must connect to an existing Pipe, Settlement, or Laboratory "
                            + "owned by " + player.getName());
        }
    }

    private boolean isConnectedToPlayerNetwork(Board board, Player player, int intersectionId) {
        Intersection intersection = board.getIntersection(intersectionId).orElse(null);
        if (intersection == null) return false;

        if (intersection.getBuilding()
                .map(b -> b.getOwnerName().equals(player.getName()))
                .orElse(false)) {
            return true;
        }

        return board.getPathsAt(intersectionId).stream()
                .anyMatch(p -> p.isOwnedBy(player.getName()));
    }
}
