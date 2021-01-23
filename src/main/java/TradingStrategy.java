/**
 * The interface responsible for realization of a trading strategy.
 */
public interface TradingStrategy {
    /**
     * Start a daytrading based on current strategy.
     */
    void trade();

    /**
     * Check if such an offer was not placed before.
     *
     * @param symbol       Stock's symbol.
     * @param qualityLong  Quantity in the offer.
     * @param closingPrice Offered price.
     * @return boolean True if such an offer was not placed.
     */
    boolean notSubmitted(String symbol, Long qualityLong, Long closingPrice);
}
