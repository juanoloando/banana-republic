package engine;

import model.board.Board;
import model.board.HexCoordinate;
import model.board.HexTile;
import model.board.Intersection;
import model.building.Building;
import model.player.Player;
import model.resource.Bank;
import model.resource.ResourceType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ResourceDistributor {

    public void distribute(Board board, List<Player> players, Bank bank,
                           int diceTotal, HexCoordinate robberPosition) {
        assert board != null : "Board must not be null";
        assert players != null && !players.isEmpty() : "Players list must not be empty";
        assert bank != null : "Bank must not be null";
        assert diceTotal >= 2 && diceTotal <= 12 : "Dice total out of range: " + diceTotal;
        assert diceTotal != 7 : "distribute() must not be called on a roll of 7";
        assert robberPosition != null : "Robber position must not be null";

        Map<ResourceType, List<PlayerYield>> yieldsByResource = new EnumMap<>(ResourceType.class);

        for (HexTile tile : board.getTilesByToken(diceTotal)) {
            if (tile.getCoordinate().equals(robberPosition)) continue;
            if (!tile.isProductive()) continue;

            ResourceType resource = tile.getProducedResource().orElse(null);
            if (resource == null) continue;

            List<Intersection> adjacentIntersections =
                    board.getIntersectionsAdjacentTo(tile.getCoordinate());

            for (Intersection intersection : adjacentIntersections) {
                intersection.getBuilding().ifPresent(building -> {
                    Player owner = findPlayer(players, building.getOwnerName());
                    if (owner != null) {
                        yieldsByResource
                                .computeIfAbsent(resource, k -> new ArrayList<>())
                                .add(new PlayerYield(owner, building.getResourceYield()));
                    }
                });
            }
        }

        for (Map.Entry<ResourceType, List<PlayerYield>> entry : yieldsByResource.entrySet()) {
            ResourceType resource = entry.getKey();
            List<PlayerYield> yields = entry.getValue();

            int totalNeeded = yields.stream().mapToInt(y -> y.amount).sum();
            int available = bank.getAvailable(resource);

            if (available == 0) continue;

            if (totalNeeded <= available) {
                for (PlayerYield yield : yields) {
                    bank.takeResource(resource, yield.amount);
                    yield.player.addResource(resource, yield.amount);
                }
            } else {
                long distinctPlayers = yields.stream()
                        .map(y -> y.player.getName())
                        .distinct()
                        .count();

                if (distinctPlayers == 1) {
                    int grant = Math.min(available, totalNeeded);
                    bank.takeResource(resource, grant);
                    yields.get(0).player.addResource(resource, grant);
                }
            }
        }
    }

    public void distributeInitialResources(Board board, Player player,
                                           int secondSettlementIntersectionId, Bank bank) {
        assert board != null : "Board must not be null";
        assert player != null : "Player must not be null";
        assert bank != null : "Bank must not be null";

        Intersection intersection = board.getIntersection(secondSettlementIntersectionId)
                .orElse(null);
        if (intersection == null) return;

        for (HexTile tile : board.getAdjacentTiles(intersection)) {
            tile.getProducedResource().ifPresent(resource -> {
                if (bank.hasResource(resource, 1)) {
                    bank.takeResource(resource, 1);
                    player.addResource(resource, 1);
                }
            });
        }
    }

    private Player findPlayer(List<Player> players, String name) {
        return players.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static final class PlayerYield {
        final Player player;
        final int amount;

        PlayerYield(Player player, int amount) {
            this.player = player;
            this.amount = amount;
        }
    }
}
