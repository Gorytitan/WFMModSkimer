import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class WFMApi {
  private static final Duration MARKET_DATA_DURATION_DAYS = Duration.ofDays(7);
  private static final Instant THRESHOLD = Instant.now().minus(MARKET_DATA_DURATION_DAYS);

  private static JSONArray getModStatsJson(String modUrlName) {
    StringBuilder jsonStringBuilder = null;
    try {
      jsonStringBuilder =
          ApiCaller.getJSON("https://api.warframe.market/v1/items/" + modUrlName + "/statistics");
      Thread.sleep(400);
    } catch (IOException e) {
      throw new RuntimeException(
          "Couldn't connect to \"https://api.warframe.market/v1/items/" + modUrlName +
              "/statistics\"");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    //turn the string into a json
    JSONObject wrapper;
    try {
      JSONParser parse = new JSONParser();
      wrapper = (JSONObject) parse.parse(String.valueOf(jsonStringBuilder));
    } catch (ParseException e) {
      return null;
    }
    JSONObject payload = (JSONObject) wrapper.get("payload");
    JSONObject statisticsClosed = (JSONObject) payload.get("statistics_closed");
    return (JSONArray) statisticsClosed.get("90days");
  }

  public static double[] getModStats(String modUrlName) throws ModNotFoundException {
    JSONArray stats = getModStatsJson(modUrlName);
    if (stats == null){
      throw new ModNotFoundException(modUrlName);
    }
    if (stats == null) {
      return new double[] {0, 0};
    }
    double sumMax = 0;
    double sumUnranked = 0;
    double volMax = 0;
    double volUnranked = 0;
    for (Object stat : stats) {
      JSONObject statJson = (JSONObject) stat;


      Instant lastUpdated = Instant.parse(statJson.get("datetime").toString());

      if (lastUpdated.isBefore(THRESHOLD)) {
        continue;
      }
      if (Double.parseDouble(statJson.get("mod_rank").toString()) == 0) {
        sumUnranked += Double.parseDouble(statJson.get("wa_price").toString()) * Double.parseDouble(
            statJson.get("volume").toString());
        volUnranked += Double.parseDouble(statJson.get("volume").toString());
      } else {
        sumMax += Double.parseDouble(statJson.get("wa_price").toString()) * Double.parseDouble(
            statJson.get("volume").toString());
        volMax += Double.parseDouble(statJson.get("volume").toString());
      }
    }
    return new double[] {sumMax / Math.max(volMax, 1) - sumUnranked / Math.max(volUnranked, 1),
        volMax};
  }
}
