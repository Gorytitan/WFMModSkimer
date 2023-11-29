import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class WarframeMobileDatamined {
  public final static String MOD_PATH = "data" + File.separator + "mods";

  public static void main(String[] args) throws IOException, ParseException {
    writeMods();
  }

  public static void writeMods() throws IOException, ParseException {
    try (FileWriter writer = new FileWriter(MOD_PATH)) {
      JSONArray mods = ApiCaller.getJsonArray(
          "https://raw.githubusercontent.com/WFCD/warframe-items/master/data/json/Mods.json");
      System.out.println("Finished getting mod list");
      for (Object objectMod : mods) {
        JSONObject jsonMod = (JSONObject) objectMod;
        if (jsonMod.get("fusionLimit") != null && ((Long) jsonMod.get(
            "fusionLimit")).intValue() == 10 && !(jsonMod.get("uniqueName").toString().toLowerCase()
            .contains("expert") && !jsonMod.get("name").toString().toLowerCase()
            .contains("prime"))) {
          String urlName = jsonMod.get("name").toString().toLowerCase().replace(" ", "_");
          urlName = urlName.replace("-", "_");
          //check wfm to make sure stats match
          try {
            JSONObject wfmCheck =
                ApiCaller.getJsonObject("https://api.warframe.market/v1/items/" + urlName);
            try {
              Thread.sleep(400);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
            JSONArray wfmArr =
                (JSONArray) ((JSONObject) ((JSONObject) wfmCheck.get("payload")).get("item")).get(
                    "items_in_set");
            if (Integer.parseInt(
                ((JSONObject) (wfmArr.get(0))).get("mod_max_rank").toString()) != 10) {
              System.out.println(urlName +" womp");
              continue;
            }
          }catch (IOException | ParseException e){
            System.out.println(urlName + " fuck");
            continue;
          }
          String rarity = jsonMod.get("rarity").toString();
          writer.write(urlName + "," + rarity + "\n");
        }
      }
    }
  }

  public static Map<String, Integer> loadMods() {
    Map<String, Integer> mods = new HashMap<>();
    try (Scanner reader = new Scanner(new File(MOD_PATH))) {
      while (reader.hasNextLine()) {
        String[] data = reader.nextLine().split(",");
        int endoCost;
        switch (data[1]) {
          case "Common" -> endoCost = 10230;
          case "Uncommon" -> endoCost = 20460;
          case "Rare" -> endoCost = 30690;
          case "Legendary" -> endoCost = 40920;
          default -> throw new RuntimeException("Rarity: \"" + data[1] + "\" unhandled");
        }
        mods.put(data[0], endoCost);
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    return mods;
  }
}
