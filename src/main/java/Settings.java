import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


/**
 * Class containing static settings for ordersController read from json file
 */
public class Settings {
    public static int SHORT_PERIOD = 5;
    public static int LONG_PERIOD = 10;
    public static int AVG_AMOUNT_PERIOD = 7;
    public static int SESSION_INTERVAL = 60;

    /**
     * Read and set fields from file
     */
    public static void readFile(String filename) {
        JSONParser jsonParser = new JSONParser();
        try (Reader reader = new FileReader(filename)) {
            JSONObject jo = (JSONObject) jsonParser.parse(reader);

            SHORT_PERIOD = (int) (long) jo.get("shortPeriod");
            LONG_PERIOD = (int) (long) jo.get("longPeriod");
            AVG_AMOUNT_PERIOD = (int) (long) jo.get("avgAmountPeriod");
            SESSION_INTERVAL = (int) (long) jo.get("sessionInterval");


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
