import junit.framework.TestCase;
import model.order.Client;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriceCollectorTest {

    private static final Client client = new Client("client03");
    private static final String password = "Jwnkq3JA";

    @Test
    public void testSortedHistory(){
        MarketPlugin marketPlugin = new DefaultMarketPlugin(client, password);
        PriceCollector priceCollector = new PriceCollector(marketPlugin);
        HashMap<String, List<Long>> hashMap = priceCollector.run();
        List<Long> zelmet = hashMap.get("ZAMET");
    //    List<Long> expected = Arrays.asList(103,102,102, 102, 102, 102, 102, 101, 101, 101, 101, 101, 101, 100, 100, 100, 100, 99, 99, 99, 99, 90, 85, 85, 85, 85, 84, 83, 77, 82, 79, 83, 83, 85, 85, 84, 84, 89, 88, 87, 87, 86, 86, 99, 98, 97, 96, 95, 94, 93, 92, 91, 90, 89, 100, 88, 100, 94, 94, 94, 94, 97, 97, 103, 103, 104, 104);
     //   Assertions.assertEquals(expected, zelmet);
    }
}