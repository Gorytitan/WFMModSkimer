import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class Main {
  private static final boolean UPDATE_MODS = false;
  private static final String MAXED_MOD_PATH = "data" + File.separator + "MaxedModCalc.csv";

  public static void main(String[] args) {
    Instant start = Instant.now();
    if (UPDATE_MODS) {
      try {
        WarframeMobileDatamined.writeMods();
      } catch (IOException | ParseException e) {
        System.out.println(e.getMessage());
        return;
      }
    }
    Map<String, Integer> modMap = WarframeMobileDatamined.loadMods();
    ArrayList<Mod> modList = new ArrayList<>();
    int count = 0;
    Set<Map.Entry<String, Integer>> modSet = modMap.entrySet();
    for (Map.Entry<String, Integer> entry : modSet) {
      System.out.println(++count + "/" + modSet.size());
      try {
        Mod mod = new Mod(entry.getKey(), entry.getValue());
        modList.add(mod);
      } catch (ModNotFoundException e) {
        continue;
      }
    }
    modList.sort(Comparator.reverseOrder());
    try (FileWriter writer = new FileWriter("data" + File.separator + "MaxedModCalc.csv")){
      writer.write("Mod Name,Plat per 10k Endo,Plat per Mod,Volume\n");
      for (Mod mod : modList){
        mod.writeToCSV(writer);
      }
    } catch (IOException e) {
      throw new RuntimeException("Couldn't write to path: " + MAXED_MOD_PATH);
    }
    Instant end = Instant.now();
    System.out.println(Duration.between(start, end).toSeconds() + " seconds elasped");
  }
}
