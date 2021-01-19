package model;

import model.order.Instrument;

import java.util.Collection;

public sealed interface Instruments {
    record Correct(Collection<Instrument> available) implements Instruments {
    }

    record Failed(String message) implements Instruments {
    }
}
