package com.bananarepublic.trade;

import java.util.Set;

public class HarborTradeService {
    public String validate(
            HarborType selectedHarbor,
            Set<HarborType> availableHarbors,
            TradeResource giveResource,
            TradeResource receiveResource,
            int playerGiveAmount,
            int bankReceiveAmount) {
        if (selectedHarbor == null) {
            return "Please select a harbor.";
        }
        if (!availableHarbors.contains(selectedHarbor)) {
            return "You do not have access to this harbor.";
        }
        if (giveResource == null) {
            return "Please select a resource to give.";
        }
        if (receiveResource == null) {
            return "Please select a resource to receive.";
        }
        if (giveResource == receiveResource) {
            return "You cannot trade the same resource.";
        }

        TradeResource specialResource = selectedHarbor.specialResource();
        if (specialResource != null && giveResource != specialResource) {
            return "This harbor only accepts " + specialResource.displayName() + ".";
        }

        int requiredAmount = selectedHarbor.requiredAmount();
        if (playerGiveAmount < requiredAmount) {
            String resourceName = giveResource.displayName();
            return "Not enough " + resourceName + ". You need " + requiredAmount + " " + resourceName + ".";
        }

        if (bankReceiveAmount < 1) {
            return "Bank does not have enough " + receiveResource.displayName() + ".";
        }

        return null;
    }
}
