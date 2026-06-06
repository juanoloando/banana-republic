import plugin.PlayerStrategy;
import engine.GameState;
import model.player.Player;
import model.resource.ResourceType;
import model.resource.Bank;

/**
 * AggressiveBot – Strategi: "Rush Lab (Laboratorium)."
 *
 * Mekanisme inti:
 *   1. Goal utama: kumpulkan 3×ORE + 2×WHEAT untuk upgrade ke Lab (1 VP extra + produksi ×2).
 *   2. Resource non-goal (WOOD, BRICK, BANANA) langsung di-trade 4:1 ke ORE atau WHEAT.
 *   3. Di antara ORE dan WHEAT, prioritaskan yang paling kurang (selisih terhadap target).
 *   4. Stop begitu bisa afford Lab — GameController lalu memanggil engine.upgradeLaboratory().
 *   5. Fallback: jika bank kehabisan ORE/WHEAT, kumpulkan settlement materials
 *      (WOOD+BRICK+WHEAT+BANANA) agar setidaknya bisa bangun Settlement baru.
 *   6. TIDAK pernah mengonsumsi / mensimulasikan pembelian kartu sendiri.
 *
 * Filosofi: Lab memberikan VP dan melipatgandakan produksi resource — investasi terbaik
 * untuk kemenangan jangka panjang.
 */
public class AggressiveBot implements PlayerStrategy {

    private static final int ORE_TARGET   = 3;
    private static final int WHEAT_TARGET = 2;

    @Override
    public String getStrategyName() { return "AggressiveBot"; }

    @Override
    public void takeTurn(GameState state, Player self) {
        Bank bank = state.getBank();

        boolean traded = true;
        int safetyLimit = 30;
        while (traded && safetyLimit-- > 0) {
            traded = false;

            // Stop when we can afford a Lab – controller will upgrade.
            if (canAffordLab(self)) break;

            // Find something to trade away.
            ResourceType surplus = findBestSurplus(self);
            if (surplus == null) break;

            // Prefer the lab resource (ORE or WHEAT) that is most deficient.
            ResourceType target = findBestLabTarget(self, surplus, bank);

            // Fallback: if lab resources unavailable in bank, grab settlement materials.
            if (target == null) {
                target = findSettlementFallback(self, surplus, bank);
            }
            if (target == null) break;

            self.removeResource(surplus, 4);
            bank.returnResource(surplus, 4);
            bank.takeResource(target, 1);
            self.addResource(target, 1);
            traded = true;
        }
    }

    // ── helpers ──────────────────────────────────────────────────────

    private boolean canAffordLab(Player self) {
        return self.getResourceCount(ResourceType.ORE)   >= ORE_TARGET
            && self.getResourceCount(ResourceType.WHEAT) >= WHEAT_TARGET;
    }

    /**
     * Best resource to trade away:
     *  - Non-lab, non-goal resources first (WOOD, BRICK, BANANA) if >= 4.
     *  - Lab resources (ORE, WHEAT) only when far beyond target (excess >= 2).
     */
    private ResourceType findBestSurplus(Player self) {
        // Non-goal first
        for (ResourceType type : new ResourceType[]{
                ResourceType.WOOD, ResourceType.BRICK, ResourceType.BANANA}) {
            if (self.getResourceCount(type) >= 4) return type;
        }
        // Lab resources only if clearly in excess
        if (self.getResourceCount(ResourceType.ORE)   >= ORE_TARGET   + 2) return ResourceType.ORE;
        if (self.getResourceCount(ResourceType.WHEAT) >= WHEAT_TARGET + 2) return ResourceType.WHEAT;
        return null;
    }

    /** ORE or WHEAT – whichever is furthest below its target, if bank has it. */
    private ResourceType findBestLabTarget(Player self, ResourceType exclude, Bank bank) {
        int oreDeficit   = Math.max(0, ORE_TARGET   - self.getResourceCount(ResourceType.ORE));
        int wheatDeficit = Math.max(0, WHEAT_TARGET - self.getResourceCount(ResourceType.WHEAT));

        if (oreDeficit == 0 && wheatDeficit == 0) return null; // Already have enough

        if (oreDeficit >= wheatDeficit) {
            if (!ResourceType.ORE.equals(exclude) && bank.hasResource(ResourceType.ORE, 1))
                return ResourceType.ORE;
            if (!ResourceType.WHEAT.equals(exclude) && bank.hasResource(ResourceType.WHEAT, 1))
                return ResourceType.WHEAT;
        } else {
            if (!ResourceType.WHEAT.equals(exclude) && bank.hasResource(ResourceType.WHEAT, 1))
                return ResourceType.WHEAT;
            if (!ResourceType.ORE.equals(exclude) && bank.hasResource(ResourceType.ORE, 1))
                return ResourceType.ORE;
        }
        return null;
    }

    /** Fallback: trade toward WOOD/BRICK/WHEAT/BANANA for a Settlement. */
    private ResourceType findSettlementFallback(Player self, ResourceType exclude, Bank bank) {
        ResourceType[] priority = {
            ResourceType.WOOD, ResourceType.BRICK,
            ResourceType.WHEAT, ResourceType.BANANA
        };
        ResourceType lowest = null;
        int lowestCount = Integer.MAX_VALUE;
        for (ResourceType type : priority) {
            if (type.equals(exclude)) continue;
            if (!bank.hasResource(type, 1)) continue;
            int count = self.getResourceCount(type);
            if (count < lowestCount) { lowestCount = count; lowest = type; }
        }
        return lowest;
    }
}
