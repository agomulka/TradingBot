import model.order.Client;

public interface TradingStrategy {
    void trading();
    boolean checkIfNotSubmitted(String symbol, Long qualityLong, Long closingPrice);
}
