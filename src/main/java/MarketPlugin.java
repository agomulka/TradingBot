import model.order.Instrument;
import model.Portfolio;
import model.Processed;
import model.Submitted;
import model.History;
import model.Instruments;
import model.SubmitOrder;
import model.order.ValidatedOrder;

public interface MarketPlugin {

    Portfolio portfolio();

    ValidatedOrder buy(SubmitOrder.Buy buyOrder);

    ValidatedOrder sell(SubmitOrder.Sell sellOrder);

    History history(Instrument instrument);

    Instruments instruments();

    Submitted submitted();

    Processed processed();
}
