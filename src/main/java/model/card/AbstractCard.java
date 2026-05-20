package model.card;

/**
 * Abstract base untuk semua kartu bawaan game.
 * Menyimpan name, description, dan category agar tidak duplikat di tiap subclass (DRY).
 * Plugin eksternal boleh extend class ini atau implement ExperimentCard langsung.
 */
public abstract class AbstractCard implements ExperimentCard {

    private final String cardName;
    private final String description;
    private final CardCategory category;

    protected AbstractCard(String cardName, String description, CardCategory category) {
        this.cardName = cardName;
        this.description = description;
        this.category = category;
    }

    @Override public String getCardName()      { return cardName; }
    @Override public String getDescription()   { return description; }
    @Override public CardCategory getCategory(){ return category; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + cardName + "'}";
    }
}
