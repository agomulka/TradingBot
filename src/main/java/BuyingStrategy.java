import model.Portfolio;
import model.SubmitOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;


/**
 * The main class for buying strategy.
 * Connected to the market to trade on; uses MovingAveragesTasker.
 *
 * @param marketPlugin   Plugin to the market to trade on.
 * @param averagesTasker Tasker calculating all averages.
 */
public record BuyingStrategy(MarketPlugin marketPlugin,
                             MovingAveragesTasker averagesTasker) implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);


    /**
     * Look for an opportunity and don't miss it!
     */
    @Override
    public void trade() {
        Portfolio portfolio = marketPlugin.portfolio();

        if (portfolio instanceof Portfolio.Current pc) {
            final var cash = pc.cash();
            logger.info("Available cash: {}", cash);
        }

        HashMap<String, Double> shortAveragesLast = averagesTasker.getAveragePrices(true, true);
        HashMap<String, Double> shortAveragesBefore = averagesTasker.getAveragePrices(true, false);
        HashMap<String, Double> longAveragesLast = averagesTasker.getAveragePrices(false, true);
        HashMap<String, Double> longAveragesBefore = averagesTasker.getAveragePrices(false, false);

        for (String symbol : shortAveragesLast.keySet()) {

            final var avgS = shortAveragesLast.get(symbol);
            final var avgSB = shortAveragesBefore.get(symbol);
            final var avgL = longAveragesLast.get(symbol);
            final var avgLB = longAveragesBefore.get(symbol);

            final var signal = averagesTasker.signal(avgS, avgL) - averagesTasker.signal(avgSB, avgLB);

            if (signal == 1) {
                final var price = getLastPrice(symbol);
                final var qty = buyQty(symbol);
                if (canIBuy(symbol, portfolio, price, qty)) {
                    buy(symbol, qty, price);
                }
            }
        }
    }

    /**
     * Buy a stock.
     *
     * @param symbol Symbol of the stock.
     * @param qty    Quantity of the offer.
     * @param price  Unit price of the offer.
     */
    public void buy(String symbol, long qty, long price) {
        logger.info("Placing buy order of {} ", symbol);
        final var buy = new SubmitOrder.Buy(symbol, UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.buy(buy);
        logger.info("validated buy: {}", validatedSell);
    }

    /**
     * Calculate quantity of a stock for new offer.
     *
     * @param symbol Stock's symbol.
     * @return long Amount.
     */
    public long buyQty(String symbol) {
        return (long) averagesTasker.getAverageAmount(symbol);
    }


    /**
     * Check if there is enough money for new buy (considering open offers) and not such an offer placed before.
     *
     * @param symbol    Symbol of the stock.
     * @param portfolio Our portfolio.
     * @param price     Considered price.
     * @param qty       Considered quantity.
     * @return boolean True if I can buy.
     */
    public boolean canIBuy(String symbol, Portfolio portfolio, long price, long qty) {
        boolean contains = false;
        if (portfolio instanceof Portfolio.Current pc) {
            if (notSubmitted(portfolio, symbol, qty, price)) { // check if we have the same buy offer
                final var cash = pc.cash();  // cash in wallet
                Long blockedCash = pc.toBuy().stream()
                        .map(x -> x.bid().price() * x.bid().qty())
                        .reduce((long) 0, Long::sum);  // cost of all still open offers
                long availableCash = cash - blockedCash;    // money we can spend
                contains = price * qty < availableCash;     // we can afford it
            }
        }
        return contains;
    }

    /**
     * Get price of last transaction of a stock.
     *
     * @param symbol Symbol of the stock.
     * @return long Amount.
     */
    public long getLastPrice(String symbol) {
        return averagesTasker.getPrices().get(symbol).get(0);
    }

    /**
     * Check if such an offer was not placed before.
     *
     * @param portfolio Our portfolio.
     * @param symbol    Stock's symbol.
     * @param qty       Quantity in the offer.
     * @param price     Offered price.
     * @return boolean True if such an offer was not placed.
     */
    @Override
    public boolean notSubmitted(Portfolio portfolio, String symbol, Long qty, Long price) {
        if (portfolio instanceof Portfolio.Current pc) {
            return pc.toBuy().stream()
                    .filter(x -> x.instrument().symbol().equals(symbol)
                            && x.bid().qty() == qty && x.bid().price() == price).findAny().isEmpty();
        }
        return false;
    }
}
