import model.Portfolio;

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
     * @param portfolio Our portfolio.
     * @param symbol    Stock's symbol.
     * @param qty       Quantity in the offer.
     * @param price     Offered price.
     * @return boolean True if such an offer was not placed.
     */
    boolean notSubmitted(Portfolio portfolio, String symbol, Long qty, Long price);

}
