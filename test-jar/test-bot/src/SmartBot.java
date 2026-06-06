import plugin.PlayerStrategy;
import engine.GameState;
import model.player.Player;
import model.player.HumanPlayer;
import model.resource.ResourceType;
import model.resource.Bank;
import model.board.Board;

import java.util.*;

/**
 * SmartBot – Strategi: Adaptif + Harbor-Aware.
 *
 * Mekanisme inti:
 *   1. Evaluasi tiga goal berdasarkan "defisit" resource:
 *        - Settlement : WOOD×1 + BRICK×1 + BANANA×1 + WHEAT×1  (VP + lokasi baru)
 *        - Lab        : ORE×3  + WHEAT×2                        (VP + produksi ×2)
 *        - Pipe       : WOOD×1 + BRICK×1                        (ekspansi jaringan)
 *   2. Pilih goal dengan defisit terkecil (paling mudah dicapai sekarang).
 *   3. Stop trading begitu defisit goal = 0 — GameController akan build dengan engine.
 *      TIDAK pernah mengonsumsi resource sendiri (tidak ada phantom card-use).
 *   4. Gunakan harbor terbaik jika tersedia (ratio 3:1 atau 2:1) via
 *      HumanPlayer.getOccupiedIntersectionIds() + Board.getBestTradeRatio().
 *   5. Trade surplus resource (dipilih dengan score = count − ratio) menuju goal.
 *
 * Filosofi: Adaptif ke kondisi resource yang ada; harbor awareness membuatnya
 * lebih efisien daripada bot lain yang selalu 4:1.
 */
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

            // Pick goal with lowest current deficit.
            Map<ResourceType, Integer> goal = pickBestGoal(self, bank);

            // If goal is already fully funded, stop – let the controller build.
            if (computeDeficit(self, goal) == 0) break;

            // Find which goal resource we still need most.
            ResourceType needed = findMostNeeded(self, goal, bank);
            if (needed == null) break; // Bank can't supply what we need

            // Find the best surplus resource to trade away (harbor-aware score).
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

    // ── helpers ──────────────────────────────────────────────────────

    private Set<Integer> getOccupiedIntersections(Player self) {
        if (self instanceof HumanPlayer) {
            return ((HumanPlayer) self).getOccupiedIntersectionIds();
        }
        return Collections.emptySet();
    }

    /**
     * Return the goal cost-map with the smallest current resource deficit.
     * Tie-break: Settlement > Lab > Pipe (settlement is most universally valuable).
     */
    private Map<ResourceType, Integer> pickBestGoal(Player self, Bank bank) {
        int settlementDef = computeDeficit(self, SETTLEMENT_COST);
        int labDef        = computeDeficit(self, LAB_COST);
        int pipeDef       = computeDeficit(self, PIPE_COST);

        // If settlement or lab are already affordable, prefer the higher-value one.
        if (settlementDef == 0 && labDef == 0) return LAB_COST; // Both ready; pick lab (more VP)
        if (settlementDef == 0)                return SETTLEMENT_COST;
        if (labDef == 0)                        return LAB_COST;

        // Otherwise pick smallest deficit.
        if (pipeDef < settlementDef && pipeDef < labDef) return PIPE_COST;
        if (settlementDef <= labDef)                     return SETTLEMENT_COST;
        return LAB_COST;
    }

    /** Sum of max(0, required − have) across all goal resources. */
    private int computeDeficit(Player self, Map<ResourceType, Integer> cost) {
        int total = 0;
        for (Map.Entry<ResourceType, Integer> e : cost.entrySet()) {
            int have = self.getResourceCount(e.getKey());
            if (have < e.getValue()) total += e.getValue() - have;
        }
        return total;
    }

    /** Goal resource with highest deficit that bank can still supply. */
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

    /**
     * Surplus resource to trade: score = count − ratio (higher leftover is better).
     * Skips the resource we currently need.
     */
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
