import model.Portfolio;
import model.SubmitOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;


/**
 * The main class for selling strategy.
 * Connected to the market to trade on; uses MovingAveragesTasker.
 */
public class SellingStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private final MarketPlugin marketPlugin;

    private final MovingAveragesTasker averagesTasker;

    public SellingStrategy(MarketPlugin marketPlugin, MovingAveragesTasker averagesTasker) {
        this.marketPlugin = marketPlugin;
        this.averagesTasker = averagesTasker;
    }


    /**
     * Look for an opportunity and don't miss it!
     */
    @Override
    public void trade() {
        Portfolio portfolio = marketPlugin.portfolio();

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

            if (signal == -1) {
                final var price = getLastPrice(symbol);
                final var qty = sellQty(symbol, portfolio);
                if (qty > 0 && notSubmitted(symbol, qty, price)) {
                    sell(symbol, qty, price);
                }
            }
        }
    }

    /**
     * sell a stock.
     *
     * @param symbol Symbol of the stock.
     * @param qty    Quantity of the offer.
     * @param price  Unit price of the offer.
     */
    public void sell(String symbol, long qty, long price) {
        logger.info("Placing sell order of {} ", symbol);
        final var sell = new SubmitOrder.Sell(symbol, UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.sell(sell);
        logger.info("validated sell: {}", validatedSell);
    }

    /**
     * Calculate quantity of a stock for new offer.
     *
     * @param symbol Stock's symbol.
     * @return long Amount, 0 if no stocks in portfolio.
     */
    public long sellQty(String symbol, Portfolio portfolio) {
        long amount = (long) averagesTasker.getAverageAmount(symbol);
        if (portfolio instanceof Portfolio.Current pc) {
            if (canISell(symbol, portfolio)) {
                final var qtyInPortfolio = pc.portfolio()
                        .stream().filter(sell -> sell.instrument()
                                .symbol().equals(symbol))
                        .findFirst().get().qty();
                return Math.min(amount, qtyInPortfolio);
            } else return 0;
        } else return 0;
    }


    /**
     * Check if there are stocks in portfolio.
     *
     * @param symbol    Symbol of the stock.
     * @param portfolio Our portfolio.
     * @return boolean True if I can sell.
     */
    public boolean canISell(String symbol, Portfolio portfolio) {
        boolean contains;
        if (portfolio instanceof Portfolio.Current pc) {
            contains = pc.portfolio().stream().anyMatch(s -> s.instrument().symbol().equals(symbol));
        } else contains = false;
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
     * @param symbol Stock's symbol.
     * @param qty    Quantity in the offer.
     * @param price  Offered price.
     * @return boolean True if such an offer was not placed.
     */
    @Override
    public boolean notSubmitted(String symbol, Long qty, Long price) {
        Portfolio portfolio = marketPlugin.portfolio();
        if (portfolio instanceof Portfolio.Current pc) {
            return pc.toSell().stream().filter(sell -> sell.instrument().symbol().equals(symbol)
                    && sell.ask().qty() == qty && sell.ask().price() == price)
                    .findAny().isEmpty();
        }
        return false;
    }
}
