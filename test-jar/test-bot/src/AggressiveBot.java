import plugin.PlayerStrategy;
import engine.GameState;
import model.player.Player;
import model.resource.ResourceType;
import model.resource.Bank;

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

            if (canAffordLab(self)) break;

            ResourceType surplus = findBestSurplus(self);
            if (surplus == null) break;

            ResourceType target = findBestLabTarget(self, surplus, bank);

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

    private boolean canAffordLab(Player self) {
        return self.getResourceCount(ResourceType.ORE)   >= ORE_TARGET
            && self.getResourceCount(ResourceType.WHEAT) >= WHEAT_TARGET;
    }

    private ResourceType findBestSurplus(Player self) {
        for (ResourceType type : new ResourceType[]{
                ResourceType.WOOD, ResourceType.BRICK, ResourceType.BANANA}) {
            if (self.getResourceCount(type) >= 4) return type;
        }
        if (self.getResourceCount(ResourceType.ORE)   >= ORE_TARGET   + 2) return ResourceType.ORE;
        if (self.getResourceCount(ResourceType.WHEAT) >= WHEAT_TARGET + 2) return ResourceType.WHEAT;
        return null;
    }

    private ResourceType findBestLabTarget(Player self, ResourceType exclude, Bank bank) {
        int oreDeficit   = Math.max(0, ORE_TARGET   - self.getResourceCount(ResourceType.ORE));
        int wheatDeficit = Math.max(0, WHEAT_TARGET - self.getResourceCount(ResourceType.WHEAT));

        if (oreDeficit == 0 && wheatDeficit == 0) return null;

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
