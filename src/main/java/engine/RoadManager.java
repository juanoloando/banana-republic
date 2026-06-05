package engine;

import model.board.Board;
import model.board.BoardPath;
import model.board.Intersection;
import model.player.Player;
import model.player.SpecialCard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoadManager {

    private static final int MINIMUM_ROAD_LENGTH = 5;

    public int computeLongestRoad(Board board, Player player) {
        assert board != null : "Board must not be null";
        assert player != null : "Player must not be null";

        int maxLength = 0;

        for (BoardPath path : board.getAllPaths()) {
            if (!path.isOwnedBy(player.getName())) continue;

            Set<Integer> visitedPaths = new HashSet<>();
            int length = dfs(board, player, path.getIntersectionIdA(), -1, visitedPaths);
            maxLength = Math.max(maxLength, length);

            visitedPaths.clear();
            length = dfs(board, player, path.getIntersectionIdB(), -1, visitedPaths);
            maxLength = Math.max(maxLength, length);
        }

        return maxLength;
    }

    private int dfs(Board board, Player player, int currentIntersectionId,
                    int cameFromPathId, Set<Integer> visitedPaths) {
        int maxBranch = 0;

        List<BoardPath> adjacentPaths = board.getPathsAt(currentIntersectionId);

        for (BoardPath path : adjacentPaths) {
            if (!path.isOwnedBy(player.getName())) continue;
            if (visitedPaths.contains(path.getId())) continue;
            if (path.getId() == cameFromPathId) continue;

            int nextIntersectionId = path.getOtherIntersection(currentIntersectionId);

            Intersection nextIntersection = board.getIntersection(nextIntersectionId).orElse(null);
            if (nextIntersection != null && !nextIntersection.isEmpty()) {
                String occupantName = nextIntersection.getBuilding()
                        .map(model.building.Building::getOwnerName).orElse(null);
                if (occupantName != null && !occupantName.equals(player.getName())) {
                    continue;
                }
            }

            visitedPaths.add(path.getId());
            int branchLength = 1 + dfs(board, player, nextIntersectionId, path.getId(), visitedPaths);
            visitedPaths.remove(path.getId());

            maxBranch = Math.max(maxBranch, branchLength);
        }

        return maxBranch;
    }

    public void updateLongestRoad(GameStateImpl state, Player playerWhoBuilt) {
        assert state != null : "GameState must not be null";
        assert playerWhoBuilt != null : "Player must not be null";

        Board board = state.getBoard();
        int newLength = computeLongestRoad(board, playerWhoBuilt);

        Player currentHolder = state.getSpecialCardHolder(SpecialCard.LONGEST_ROAD).orElse(null);
        int currentLongest = state.getLongestRoadLength();

        if (newLength >= MINIMUM_ROAD_LENGTH && newLength > currentLongest) {
            state.setLongestRoadLength(newLength);
            state.grantSpecialCard(SpecialCard.LONGEST_ROAD, playerWhoBuilt);
        }
    }

    public void checkRoadBreak(GameStateImpl state) {
        assert state != null : "GameState must not be null";

        Player currentHolder = state.getSpecialCardHolder(SpecialCard.LONGEST_ROAD).orElse(null);
        if (currentHolder == null) return;

        Board board = state.getBoard();
        int holderLength = computeLongestRoad(board, currentHolder);

        int maxOtherLength = 0;
        Player challenger = null;
        for (Player player : state.getAllPlayers()) {
            if (player.getName().equals(currentHolder.getName())) continue;
            int len = computeLongestRoad(board, player);
            if (len > maxOtherLength) {
                maxOtherLength = len;
                challenger = player;
            }
        }

        if (holderLength < MINIMUM_ROAD_LENGTH) {
            if (maxOtherLength >= MINIMUM_ROAD_LENGTH && challenger != null) {
                state.setLongestRoadLength(maxOtherLength);
                state.grantSpecialCard(SpecialCard.LONGEST_ROAD, challenger);
            } else {
                state.setLongestRoadLength(holderLength);
                state.revokeSpecialCard(SpecialCard.LONGEST_ROAD);
            }
        } else if (maxOtherLength > holderLength && challenger != null) {
            state.setLongestRoadLength(maxOtherLength);
            state.grantSpecialCard(SpecialCard.LONGEST_ROAD, challenger);
        } else if (maxOtherLength == holderLength && maxOtherLength >= MINIMUM_ROAD_LENGTH) {
            // Tie after road break — per spec, card is revoked (nobody holds it)
            state.setLongestRoadLength(holderLength);
            state.revokeSpecialCard(SpecialCard.LONGEST_ROAD);
        } else {
            state.setLongestRoadLength(holderLength);
        }
    }

    public void updateLargestArmy(GameStateImpl state, Player playerWhoPlayedKnight) {
        assert state != null : "GameState must not be null";
        assert playerWhoPlayedKnight != null : "Player must not be null";

        int knightsPlayed = 0;
        if (playerWhoPlayedKnight instanceof model.player.HumanPlayer) {
            knightsPlayed = ((model.player.HumanPlayer) playerWhoPlayedKnight)
                    .getInventory().getKnightCardsPlayed();
        }
        int currentLargest = state.getLargestArmySize();
        Player currentHolder = state.getSpecialCardHolder(SpecialCard.LARGEST_ARMY).orElse(null);

        boolean holderIsThisPlayer = currentHolder != null
                && currentHolder.getName().equals(playerWhoPlayedKnight.getName());

        if (knightsPlayed >= 3) {
            if (currentHolder == null) {
                state.setLargestArmySize(knightsPlayed);
                state.grantSpecialCard(SpecialCard.LARGEST_ARMY, playerWhoPlayedKnight);
            } else if (!holderIsThisPlayer && knightsPlayed > currentLargest) {
                state.setLargestArmySize(knightsPlayed);
                state.grantSpecialCard(SpecialCard.LARGEST_ARMY, playerWhoPlayedKnight);
            } else if (holderIsThisPlayer) {
                state.setLargestArmySize(knightsPlayed);
            }
        }
    }

    public int getMinimumRoadLength() {
        return MINIMUM_ROAD_LENGTH;
    }
}
