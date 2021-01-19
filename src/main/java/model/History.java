package model;

import model.order.ProcessedOrder;

import java.util.Collection;

public interface History {
    record Correct(Collection<ProcessedOrder.Bought> bought, Collection<ProcessedOrder.Sold> sold) implements History {
    }

    record Failed(String message) implements History {

    }
}
