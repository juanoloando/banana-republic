import plugin.PlayerStrategy;
import engine.GameState;
import model.player.Player;
import model.player.HumanPlayer;
import model.resource.ResourceType;
import model.resource.Bank;
import model.board.Board;

import java.util.*;

public class SmartBot implements PlayerStrategy {

    private static final Map<ResourceType, Integer> SETTLEMENT_COST;
    private static final Map<ResourceType, Integer> LAB_COST;
    private static final Map<ResourceType, Integer> PIPE_COST;

    static {
        SETTLEMENT_COST = new EnumMap<>(ResourceType.class);
        SETTLEMENT_COST.put(ResourceType.WOOD,   1);
        SETTLEMENT_COST.put(ResourceType.BRICK,  1);
        SETTLEMENT_COST.put(ResourceType.BANANA, 1);
        SETTLEMENT_COST.put(ResourceType.WHEAT,  1);

        LAB_COST = new EnumMap<>(ResourceType.class);
        LAB_COST.put(ResourceType.ORE,   3);
        LAB_COST.put(ResourceType.WHEAT, 2);

        PIPE_COST = new EnumMap<>(ResourceType.class);
        PIPE_COST.put(ResourceType.WOOD,  1);
        PIPE_COST.put(ResourceType.BRICK, 1);
    }

    @Override
    public String getStrategyName() { return "SmartBot"; }

    @Override
    public void takeTurn(GameState state, Player self) {
        Bank  bank  = state.getBank();
        Board board = state.getBoard();
        Set<Integer> occupiedIds = getOccupiedIntersections(self);

        boolean traded = true;
        int safetyLimit = 30;
        while (traded && safetyLimit-- > 0) {
            traded = false;

            Map<ResourceType, Integer> goal = pickBestGoal(self, bank);

            if (computeDeficit(self, goal) == 0) break;

            ResourceType needed = findMostNeeded(self, goal, bank);
            if (needed == null) break;

            ResourceType surplus = findBestSurplus(self, needed, board, occupiedIds);
            if (surplus == null) break;

            int ratio = board.getBestTradeRatio(surplus, occupiedIds);
            if (self.getResourceCount(surplus) < ratio) break;
            if (!bank.hasResource(needed, 1)) break;

            self.removeResource(surplus, ratio);
            bank.returnResource(surplus, ratio);
            bank.takeResource(needed, 1);
            self.addResource(needed, 1);
            traded = true;
        }
    }

    private Set<Integer> getOccupiedIntersections(Player self) {
        if (self instanceof HumanPlayer) {
            return ((HumanPlayer) self).getOccupiedIntersectionIds();
        }
        return Collections.emptySet();
    }

    private Map<ResourceType, Integer> pickBestGoal(Player self, Bank bank) {
        int settlementDef = computeDeficit(self, SETTLEMENT_COST);
        int labDef        = computeDeficit(self, LAB_COST);
        int pipeDef       = computeDeficit(self, PIPE_COST);

        if (settlementDef == 0 && labDef == 0) return LAB_COST;
        if (settlementDef == 0)                return SETTLEMENT_COST;
        if (labDef == 0)                        return LAB_COST;

        if (pipeDef < settlementDef && pipeDef < labDef) return PIPE_COST;
        if (settlementDef <= labDef)                     return SETTLEMENT_COST;
        return LAB_COST;
    }

    private int computeDeficit(Player self, Map<ResourceType, Integer> cost) {
        int total = 0;
        for (Map.Entry<ResourceType, Integer> e : cost.entrySet()) {
            int have = self.getResourceCount(e.getKey());
            if (have < e.getValue()) total += e.getValue() - have;
        }
        return total;
    }

    private ResourceType findMostNeeded(Player self,
                                        Map<ResourceType, Integer> goal,
                                        Bank bank) {
        ResourceType most = null;
        int maxDef = 0;
        for (Map.Entry<ResourceType, Integer> e : goal.entrySet()) {
            int deficit = e.getValue() - self.getResourceCount(e.getKey());
            if (deficit > maxDef && bank.hasResource(e.getKey(), 1)) {
                maxDef = deficit; most = e.getKey();
            }
        }
        return most;
    }

    private ResourceType findBestSurplus(Player self,
                                          ResourceType needed,
                                          Board board,
                                          Set<Integer> occupiedIds) {
        ResourceType best = null;
        int bestScore = Integer.MIN_VALUE;
        for (ResourceType type : ResourceType.values()) {
            if (type.equals(needed)) continue;
            int count = self.getResourceCount(type);
            int ratio = board.getBestTradeRatio(type, occupiedIds);
            if (count >= ratio) {
                int score = count - ratio;
                if (score > bestScore) { bestScore = score; best = type; }
            }
        }
        return best;
    }
}
