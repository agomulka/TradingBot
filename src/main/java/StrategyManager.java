import model.order.Client;

   // niepotrzebne, będzie w OrdersController
public class StrategyManager {

    private MarketPlugin marketPlugin;
    private Client client;

    public StrategyManager(MarketPlugin marketPlugin, Client client) {
        this.marketPlugin = marketPlugin;
        this.client = client;
    }


}
