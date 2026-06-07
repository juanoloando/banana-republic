import model.card.CardCategory;
import model.card.ExperimentCard;
import model.card.GameStateContext;
import model.player.Player;
import model.resource.ResourceType;

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
        if (state.getBank().hasResource(ResourceType.WOOD, 2)) {
            state.giveResourceToPlayer(player, ResourceType.WOOD, 2);
        } else if (state.getBank().hasResource(ResourceType.WOOD, 1)) {
            state.giveResourceToPlayer(player, ResourceType.WOOD, 1);
        }
    }

    @Override
    public boolean isRevealedWhenPlayed() {
        return true;
    }
}
