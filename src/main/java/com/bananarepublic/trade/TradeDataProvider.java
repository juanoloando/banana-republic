package com.bananarepublic.trade;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TradeDataProvider {
    int getCurrentPlayerIndex();

    List<String> getPlayerNames();

    Map<TradeResource, Integer> getPlayerResources(int playerIndex);

    Map<TradeResource, Integer> getBankResources();

    Set<HarborType> getAvailableHarbors(int playerIndex);

    void addPlayerResource(int playerIndex, TradeResource resource, int amount);

    void addBankResource(TradeResource resource, int amount);

    boolean isTradeBuildPhase();
}
