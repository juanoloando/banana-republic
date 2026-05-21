package engine;

import model.board.Board;
import model.board.HexCoordinate;
import model.board.Intersection;
import model.player.Player;
import model.resource.ResourceType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RobberManager {

    private static final int DISCARD_THRESHOLD = 7;
    private final Random random;

    public RobberManager() {
        this.random = new Random();
    }

    public List<Player> getPlayersRequiredToDiscard(List<Player> players) {
        assert players != null : "Players list must not be null";
        List<Player> result = new ArrayList<>();
        for (Player player : players) {
            if (player.getTotalResourceCount() > DISCARD_THRESHOLD) {
                result.add(player);
            }
        }
        return result;
    }

    public int computeDiscardCount(Player player) {
        assert player != null : "Player must not be null";
        int total = player.getTotalResourceCount();
        assert total > DISCARD_THRESHOLD
                : "computeDiscardCount called but player " + player.getName()
                  + " has only " + total + " resources (threshold=" + DISCARD_THRESHOLD + ")";
        return total / 2;
    }

    public void discardResources(Player player, Map<ResourceType, Integer> toDiscard,
                                 model.resource.Bank bank) {
        assert player != null : "Player must not be null";
        assert toDiscard != null : "Discard map must not be null";
        assert bank != null : "Bank must not be null";

        int totalToDiscard = toDiscard.values().stream().mapToInt(Integer::intValue).sum();
        int required = computeDiscardCount(player);
        assert totalToDiscard == required
                : "Player " + player.getName() + " must discard exactly " + required
                  + " resources, got: " + totalToDiscard;

        for (Map.Entry<ResourceType, Integer> entry : toDiscard.entrySet()) {
            if (entry.getValue() > 0) {
                player.removeResource(entry.getKey(), entry.getValue());
                bank.returnResource(entry.getKey(), entry.getValue());
            }
        }
    }

    public void moveRobber(GameStateImpl state, HexCoordinate newPosition) {
        assert state != null : "GameState must not be null";
        assert newPosition != null : "New robber position must not be null";
        assert !newPosition.equals(state.getRobberPosition())
                : "Robber must move to a different tile than its current position";

        boolean tileExists = state.getBoard().getAllTiles().stream()
                .anyMatch(t -> t.getCoordinate().equals(newPosition));
        assert tileExists : "Target position " + newPosition + " does not correspond to any tile";

        state.setRobberPosition(newPosition);
    }

    public List<Player> getStealCandidates(Board board, List<Player> players,
                                            Player activePlayer, HexCoordinate robberPosition) {
        assert board != null : "Board must not be null";
        assert players != null : "Players list must not be null";
        assert activePlayer != null : "Active player must not be null";
        assert robberPosition != null : "Robber position must not be null";

        List<Intersection> adjacentIntersections =
                board.getIntersectionsAdjacentTo(robberPosition);

        List<Player> candidates = new ArrayList<>();
        for (Intersection intersection : adjacentIntersections) {
            intersection.getBuilding().ifPresent(building -> {
                String ownerName = building.getOwnerName();
                if (ownerName.equals(activePlayer.getName())) return;

                Player owner = players.stream()
                        .filter(p -> p.getName().equals(ownerName))
                        .findFirst()
                        .orElse(null);

                if (owner != null && owner.getTotalResourceCount() > 0
                        && !candidates.contains(owner)) {
                    candidates.add(owner);
                }
            });
        }
        return candidates;
    }

    public ResourceType stealRandomResource(Player activePlayer, Player targetPlayer,
                                             model.resource.Bank bank) {
        assert activePlayer != null : "Active player must not be null";
        assert targetPlayer != null : "Target player must not be null";
        assert targetPlayer.getTotalResourceCount() > 0
                : "Cannot steal from player with no resources: " + targetPlayer.getName();

        Map<ResourceType, Integer> available = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            int count = targetPlayer.getResourceCount(type);
            if (count > 0) available.put(type, count);
        }

        int total = available.values().stream().mapToInt(Integer::intValue).sum();
        int pick = random.nextInt(total);
        int cumulative = 0;
        ResourceType stolen = null;

        for (Map.Entry<ResourceType, Integer> entry : available.entrySet()) {
            cumulative += entry.getValue();
            if (pick < cumulative) {
                stolen = entry.getKey();
                break;
            }
        }

        assert stolen != null : "Failed to pick a resource to steal";

        targetPlayer.removeResource(stolen, 1);
        activePlayer.addResource(stolen, 1);
        return stolen;
    }
}
