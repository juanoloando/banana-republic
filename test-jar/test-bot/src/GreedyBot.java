import plugin.PlayerStrategy;
import engine.GameState;
import model.player.Player;
import model.resource.ResourceType;
import model.resource.Bank;

public class GreedyBot implements PlayerStrategy {

    @Override
    public String getStrategyName() { return "GreedyBot"; }

    @Override
    public void takeTurn(GameState state, Player self) {
        Bank bank = state.getBank();

        boolean traded = true;
        int safetyLimit = 25;
        while (traded && safetyLimit-- > 0) {
            traded = false;

            if (canAffordSettlement(self)) break;

            ResourceType surplus = findSurplus(self, 4);
            if (surplus == null) break;

            ResourceType deficit = findSettlementDeficit(self, surplus, bank);
            if (deficit == null) break;

            self.removeResource(surplus, 4);
            bank.returnResource(surplus, 4);
            bank.takeResource(deficit, 1);
            self.addResource(deficit, 1);
            traded = true;
        }
    }

    private boolean canAffordSettlement(Player self) {
        return self.getResourceCount(ResourceType.WOOD)   >= 1
            && self.getResourceCount(ResourceType.BRICK)  >= 1
            && self.getResourceCount(ResourceType.WHEAT)  >= 1
            && self.getResourceCount(ResourceType.BANANA) >= 1;
    }

    private ResourceType findSurplus(Player self, int threshold) {
        ResourceType best = null;
        int maxCount = threshold - 1;
        for (ResourceType type : ResourceType.values()) {
            int count = self.getResourceCount(type);
            if (count > maxCount) { maxCount = count; best = type; }
        }
        return best;
    }

    private ResourceType findSettlementDeficit(Player self, ResourceType exclude, Bank bank) {
        ResourceType[] priority = {
            ResourceType.WOOD, ResourceType.BRICK,
            ResourceType.WHEAT, ResourceType.BANANA, ResourceType.ORE
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
