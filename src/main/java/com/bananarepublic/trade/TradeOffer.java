package com.bananarepublic.trade;

import java.util.EnumMap;

import model.resource.ResourceType;

public class TradeOffer {
    public int proposerIndex;
    public int receiverIndex;
    public final EnumMap<ResourceType, Integer> give = new EnumMap<>(ResourceType.class);
    public final EnumMap<ResourceType, Integer> receive = new EnumMap<>(ResourceType.class);
    public boolean counter;

    public TradeOffer copy() {
        TradeOffer copy = new TradeOffer();
        copy.proposerIndex = proposerIndex;
        copy.receiverIndex = receiverIndex;
        copy.give.putAll(give);
        copy.receive.putAll(receive);
        copy.counter = counter;
        return copy;
    }
}
