package model.card;

public abstract class ProgressCard extends AbstractCard {

    protected ProgressCard(String cardName, String description) {
        super(cardName, description, CardCategory.PROGRESS);
    }

    @Override
    public boolean isRevealedWhenPlayed() {
        return true;
    }
}
