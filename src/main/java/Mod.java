import java.io.FileWriter;
import java.io.IOException;

public class Mod implements Comparable {
  final protected String urlName;
  final protected double endoCost;
  protected double endoPlatRatio;
  protected double platPerMod;
  protected int volume;

  public Mod(String urlName, int endoCost) throws ModNotFoundException {
    this.urlName = urlName;
    this.endoCost = endoCost;
    double[] stats = WFMApi.getModStats(urlName);
    updateRatio(stats);
  }

  public void updateRatio(double[] stats) {
    endoPlatRatio = stats[0] / endoCost * 10000;
    platPerMod = stats[0];
    volume = (int) stats[1];
  }

  @Override
  public String toString() {
    return urlName + ", " + endoPlatRatio;
  }

  @Override
  public int compareTo(Object o) {
    if (!(o instanceof Mod other)) {
      return 1;
    }
    if (this.endoPlatRatio == other.endoPlatRatio) {
      return 0;
    }
    return this.endoPlatRatio > other.endoPlatRatio ? 1 : -1;
  }

  public void writeToCSV(FileWriter writer) {
    try {
      writer.write(urlName + "," + endoPlatRatio +","+platPerMod+","+volume+"\n");
    } catch (IOException e) {
      System.out.println("Couldn't write " + urlName + " to file");
    }
  }
}
