import model.*;
import model.order.Client;
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
        Tasks task = new Tasks(marketPlugin);
        Instruments instruments = marketPlugin.instruments();
        Portfolio portfolio = marketPlugin.portfolio();


        if (portfolio instanceof Portfolio.Current pc) {
            final var cash = pc.cash();
            logger.info("Available cash: {}", cash);
        }

        if (instruments instanceof Instruments.Correct ic) {
            for (Instrument instr : ic.available()) {

                final var avgS = task.getAverage(instr, 5);
                final var avgSB = task.getAverageBefore(instr, 5);
                final var avgL = task.getAverage(instr, 30);
                final var avgLB = task.getAverageBefore(instr, 30);

                final var position = task.signal(avgS, avgL) - task.signal(avgSB, avgLB);

                if (position == -1 && task.CanISell(instr, portfolio)) {
                    final var price = task.getPrice(instr);
                    final var qty = task.SellQty(instr, portfolio);
                    task.Sell(instr, qty, (long)(1.1*price));
                } else

                    if (position == 1) {
                    final var price = task.getPrice(instr);
                    final var qty = task.BuyQty(instr, portfolio);
                    task.Buy(instr, qty, price);
                }
            }
        }
    }
}

