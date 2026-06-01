package com.bananarepublic.cards;

public class CardDefinition {
    private final CardType type;
    private final CardCategory category;
    private final String displayName;
    private final PlayabilityType playabilityType;
    private final String templateImagePath;
    private final boolean secretUntilReveal;

    public CardDefinition(CardType type,
                          CardCategory category,
                          String displayName,
                          PlayabilityType playabilityType,
                          String templateImagePath,
                          boolean secretUntilReveal) {
        this.type = type;
        this.category = category;
        this.displayName = displayName;
        this.playabilityType = playabilityType;
        this.templateImagePath = templateImagePath;
        this.secretUntilReveal = secretUntilReveal;
    }

    public CardType getType() {
        return type;
    }

    public CardCategory getCategory() {
        return category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PlayabilityType getPlayabilityType() {
        return playabilityType;
    }

    public String getTemplateImagePath() {
        return templateImagePath;
    }

    public boolean isSecretUntilReveal() {
        return secretUntilReveal;
    }
}

