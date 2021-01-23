public interface TradingStrategy {
    void trade();
    boolean checkIfNotSubmitted(String symbol, Long qualityLong, Long closingPrice);
  //  boolean canI(String symbol, Portfolio portfolio, long price, long qty);
}
