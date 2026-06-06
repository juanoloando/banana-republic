package com.bananarepublic.trade;

import java.util.Map;

public class BankTradeService {
    public String validate(
            TradeResource bankReceiveResource,
            TradeResource playerGiveResource,
            Map<TradeResource, Integer> playerResources,
            Map<TradeResource, Integer> bankResources) {
        if (bankReceiveResource == null) {
            return "Please select a resource from bank.";
        }
        if (playerGiveResource == null) {
            return "Please select a resource from you.";
        }
        if (bankReceiveResource == playerGiveResource) {
            return "You cannot trade the same resource.";
        }

        int playerAmount = playerResources.getOrDefault(playerGiveResource, 0);
        if (playerAmount < 4) {
            String name = playerGiveResource.displayName();
            return "Not enough " + name + ". You need 4 " + name + ".";
        }

        int bankAmount = bankResources.getOrDefault(bankReceiveResource, 0);
        if (bankAmount < 1) {
            return "Bank does not have enough " + bankReceiveResource.displayName() + ".";
        }

        return null;
    }

    public TradeResult execute(TradeDataProvider provider, TradeResource playerGiveResource, TradeResource bankReceiveResource) {
        String error = validate(
                bankReceiveResource,
                playerGiveResource,
                provider.getPlayerResources(provider.getCurrentPlayerIndex()),
                provider.getBankResources());
        if (error != null) {
            return TradeResult.failure(error);
        }

        int playerIndex = provider.getCurrentPlayerIndex();
        provider.addPlayerResource(playerIndex, playerGiveResource, -4);
        provider.addBankResource(playerGiveResource, 4);
        provider.addBankResource(bankReceiveResource, -1);
        provider.addPlayerResource(playerIndex, bankReceiveResource, 1);

        return TradeResult.success("Bank trade successful!");
    }
}
