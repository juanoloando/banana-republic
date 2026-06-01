import model.card.CardCategory;
import model.card.ExperimentCard;
import model.card.GameStateContext;
import model.player.Player;
import model.resource.ResourceType;

/**
 * Test plugin card: take 1 of any single resource type from the bank.
 */
public class WildcardCard implements ExperimentCard {

    @Override
    public String getCardName() {
        return "Wildcard";
    }

    @Override
    public String getDescription() {
        return "Take 1 Banana from the bank immediately.";
    }

    @Override
    public CardCategory getCategory() {
        return CardCategory.PROGRESS;
    }

    @Override
    public void applyEffect(GameStateContext state, Player player) {
        if (state.getBank().hasResource(ResourceType.BANANA, 1)) {
            state.giveResourceToPlayer(player, ResourceType.BANANA, 1);
        }
    }

    @Override
    public boolean isRevealedWhenPlayed() {
        return true;
    }
}
