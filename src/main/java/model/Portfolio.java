package model;

import model.order.Instrument;
import model.order.SubmittedOrder;

import java.util.Collection;

public sealed interface Portfolio {
    record Current(Collection<PortfolioElement> portfolio, Collection<SubmittedOrder.Buy> toBuy,
                   Collection<SubmittedOrder.Sell> toSell,
                   Long cash) implements Portfolio {
    }

    record Failed(String message) implements Portfolio {
    }

    record PortfolioElement(Instrument instrument, long qty){
    }
}
