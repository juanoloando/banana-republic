package com.bananarepublic.trade;

import java.util.EnumMap;
import java.util.List;

import model.resource.ResourceType;

public class PlayerTradeService {
    public String validate(
            int proposerIndex,
            int targetIndex,
            List<EnumMap<ResourceType, Integer>> playerResourceStocks,
            EnumMap<ResourceType, Integer> giveQuantities,
            EnumMap<ResourceType, Integer> receiveQuantities,
            List<ResourceType> resourceOrder) {
        if (targetIndex < 0 || targetIndex >= playerResourceStocks.size() || targetIndex == proposerIndex) {
            return "Please select a target player.";
        }
        if (sumResourceQuantities(giveQuantities) <= 0) {
            return "Please select at least one resource to give.";
        }
        if (sumResourceQuantities(receiveQuantities) <= 0) {
            return "Please select at least one resource to receive.";
        }
        for (ResourceType resourceType : resourceOrder) {
            if (giveQuantities.getOrDefault(resourceType, 0) > 0
                    && receiveQuantities.getOrDefault(resourceType, 0) > 0) {
                return "You cannot trade the same resource.";
            }
        }
        for (ResourceType resourceType : resourceOrder) {
            int askGive = giveQuantities.getOrDefault(resourceType, 0);
            if (askGive > getResourceAmount(playerResourceStocks, proposerIndex, resourceType)) {
                return "Not enough " + displayResource(resourceType) + ".";
            }
        }
        for (ResourceType resourceType : resourceOrder) {
            int askReceive = receiveQuantities.getOrDefault(resourceType, 0);
            if (askReceive > getResourceAmount(playerResourceStocks, targetIndex, resourceType)) {
                return "Target player does not have enough " + displayResource(resourceType) + ".";
            }
        }

        return null;
    }

    public boolean canExecuteOffer(TradeOffer offer, List<EnumMap<ResourceType, Integer>> playerResourceStocks) {
        if (offer == null) {
            return false;
        }
        return playerHasEnough(playerResourceStocks, offer.proposerIndex, offer.give)
                && playerHasEnough(playerResourceStocks, offer.receiverIndex, offer.receive);
    }

    private boolean playerHasEnough(
            List<EnumMap<ResourceType, Integer>> playerResourceStocks,
            int playerIndex,
            EnumMap<ResourceType, Integer> requiredResources) {
        for (ResourceType resourceType : ResourceType.values()) {
            if (getResourceAmount(playerResourceStocks, playerIndex, resourceType)
                    < requiredResources.getOrDefault(resourceType, 0)) {
                return false;
            }
        }
        return true;
    }

    private int sumResourceQuantities(EnumMap<ResourceType, Integer> quantities) {
        return quantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    private int getResourceAmount(
            List<EnumMap<ResourceType, Integer>> playerResourceStocks,
            int playerIndex,
            ResourceType resourceType) {
        if (playerIndex < 0 || playerIndex >= playerResourceStocks.size()) {
            return 0;
        }
        return playerResourceStocks.get(playerIndex).getOrDefault(resourceType, 0);
    }

    private String displayResource(ResourceType resourceType) {
        return TradeResource.fromModel(resourceType).displayName();
    }
}
