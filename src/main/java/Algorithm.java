import model.*;
import model.order.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Algorithm {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private final MarketPlugin marketPlugin;


    public Algorithm(MarketPlugin marketPlugin) {
        this.marketPlugin = marketPlugin;
    }

    public void run() {
        MovingAveragesTasker theStrangeThing = new MovingAveragesTasker(marketPlugin);
        Instruments instruments = marketPlugin.instruments();
        Portfolio portfolio = marketPlugin.portfolio();


        if (portfolio instanceof Portfolio.Current pc) {
            final var cash = pc.cash();
            logger.info("Available cash: {}", cash);
        }

        if (instruments instanceof Instruments.Correct ic) {
            for (Instrument instrument : ic.available()) {

                final var avgS = theStrangeThing.getAverage(instrument, 5);
                final var avgSB = theStrangeThing.getAverageBefore(instrument, 5);
                final var avgL = theStrangeThing.getAverage(instrument, 30);
                final var avgLB = theStrangeThing.getAverageBefore(instrument, 30);

                final var position = theStrangeThing.signal(avgS, avgL) - theStrangeThing.signal(avgSB, avgLB);

                if (position == -1 && theStrangeThing.CanISell(instrument, portfolio)) {
                    final var price = theStrangeThing.getPrice(instrument);
                    final var qty = theStrangeThing.SellQty(instrument, portfolio);
                    theStrangeThing.Sell(instrument, qty, (long)(1.1*price));
                } else

                    if (position == 1) {
                    final var price = theStrangeThing.getPrice(instrument);
                    final var qty = theStrangeThing.BuyQty(instrument, portfolio);
                    theStrangeThing.Buy(instrument, qty, price);
                }
            }
        }
    }
}
