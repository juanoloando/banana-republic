import model.card.CardCategory;
import model.card.ExperimentCard;
import model.card.GameStateContext;
import model.player.Player;
import model.resource.ResourceType;

/**
 * Test plugin card: gives the active player 2 Wood from the bank.
 * Load this JAR via Settings > Load Card Plugin to test the plugin system.
 */
public class BonusResourceCard implements ExperimentCard {

    @Override
    public String getCardName() {
        return "Bonus Resource";
    }

    @Override
    public String getDescription() {
        return "Take 2 Wood from the bank immediately.";
    }

    @Override
    public CardCategory getCategory() {
        return CardCategory.PROGRESS;
    }

    @Override
    public void applyEffect(GameStateContext state, Player player) {
        // Give the active player 2 Wood from the bank if available
        if (state.getBank().hasResource(ResourceType.WOOD, 2)) {
            state.giveResourceToPlayer(player, ResourceType.WOOD, 2);
        } else if (state.getBank().hasResource(ResourceType.WOOD, 1)) {
            state.giveResourceToPlayer(player, ResourceType.WOOD, 1);
        }
        // (No action if bank has no Wood)
    }

    @Override
    public boolean isRevealedWhenPlayed() {
        return true;
    }
}
