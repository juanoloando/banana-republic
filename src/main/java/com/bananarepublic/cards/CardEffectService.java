package com.bananarepublic.cards;

import model.resource.ResourceType;

public class CardEffectService {
    private final CardDataProvider dataProvider;
    private final UsedCardTracker usedCardTracker;

    public CardEffectService(CardDataProvider dataProvider, UsedCardTracker usedCardTracker) {
        this.dataProvider = dataProvider;
        this.usedCardTracker = usedCardTracker;
    }

    public CardUseResult useCard(CardType type, ResourceType monopolyResource) {
        CardDefinition definition = CardDefinitions.get(type);
        if (definition == null) return CardUseResult.fail("Unknown card.");

        int currentPlayerIndex = dataProvider.getCurrentPlayerIndex();
        PlayerCardInventory inventory = dataProvider.getCardInventory(currentPlayerIndex);
        if (inventory == null) return CardUseResult.fail("Card inventory is not available.");

        if (!inventory.hasCard(type)) {
            return CardUseResult.fail("You do not have this card.");
        }

        if (definition.getPlayabilityType() == PlayabilityType.PASSIVE) {
            return CardUseResult.fail("This card cannot be actively played.");
        }

        if (definition.getPlayabilityType() == PlayabilityType.REVEAL_ONLY) {
            if (dataProvider.getPlayerVictoryPoints(currentPlayerIndex) >= dataProvider.getVictoryThreshold()) {
                return CardUseResult.success("Victory Point Card revealed.", true, false);
            }
            return CardUseResult.fail("This Victory Point card cannot be revealed now.");
        }

        if (usedCardTracker.hasPlayedDiscoveryCardThisTurn(currentPlayerIndex)) {
            return CardUseResult.fail("You may only play 1 Discovery Card per turn.");
        }

        if (inventory.isBoughtThisTurn(type)) {
            return CardUseResult.fail("This card was bought this turn and cannot be used yet.");
        }

        if (type == CardType.MONOPOLY && monopolyResource == null) {
            return CardUseResult.fail("Monopoly requires a resource type selection.");
        }

        if (type == CardType.ROAD_BUILDING && dataProvider.isRoadBuildingEffectInProgress(currentPlayerIndex)) {
            return CardUseResult.fail("Road Building effect is already in progress.");
        }

        CardUseResult playResult = dataProvider.playCard(type, monopolyResource);
        if (!playResult.isSuccess()) {
            return playResult;
        }

        inventory.removeCard(type, 1);
        usedCardTracker.markDiscoveryCardPlayedThisTurn(currentPlayerIndex);

        if (type == CardType.KNIGHT) {
            usedCardTracker.incrementKnightCount(currentPlayerIndex);
            return CardUseResult.success("Knight Card used successfully.", true, true);
        }
        if (type == CardType.ROAD_BUILDING) {
            return CardUseResult.success("Road Building activated. Place 2 free Pipes.", true, true);
        }
        if (type == CardType.MONOPOLY) {
            return CardUseResult.success("Monopoly used successfully.", true, false);
        }

        return CardUseResult.success("Card used successfully.", true, false);
    }
}

