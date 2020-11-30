import java.util.HashMap;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */


public class Metric {
    private HashMap<Integer, Double> sumMetric; //Sum of all values for the tops metrics
    private HashMap<Integer, Double> lastValues; //Last value for each top

    public Metric() {
        sumMetric = new HashMap<>();
        lastValues = new HashMap<>();
        //Initialize hashmaps
        sumMetric.put(10, 0.0);
        sumMetric.put(20, 0.0);
        sumMetric.put(50, 0.0);
        lastValues.put(10, 0.0);
        lastValues.put(20, 0.0);
        lastValues.put(50, 0.0);
    }

    public double getSumMetric(int top) {
        return sumMetric.get(top);
    }

    public double getLastValue(int top){
        return lastValues.get(top);
    }

    public void addNewValue(int top, double value){
        sumMetric.replace(top, sumMetric.get(top) + value);
        lastValues.replace(top, value);
    }
}
