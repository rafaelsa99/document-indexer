import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */


public class Metrics {

    HashMap<Integer, HashMap<String, Integer>> query_Filter; //<QueryID, HashMap<Cord_UI, Relevance>>
    ArrayList<Integer> latencies;
    Metric precisions;
    Metric recalls;
    Metric fmeasures;
    Metric averagePrecisions;
    Metric ndcgs;

    int numQueries;
    int tp; //Retrieved -> Relevant
    int fp; //Retrieved -> NonRelevant
    int tn; //Not Retrieved -> NonRelevant
    int fn; //Not Retrieved -> Relevant

    BufferedWriter writerMetrics;

    public Metrics(String relevancesFilename, String metricsFilename) throws IOException {
        query_Filter = new HashMap<>();
        readFilterQueryfile(relevancesFilename);
        this.fmeasures = new Metric();
        this.precisions = new Metric();
        this.recalls = new Metric();
        this.averagePrecisions = new Metric();
        this.ndcgs = new Metric();
        this.latencies = new ArrayList<>();
        this.numQueries = 0;
        initializeMetricsFile(metricsFilename);
    }

    private void initializeMetricsFile(String metricsFilename) throws IOException {
        File queryfile = new File(metricsFilename);
        writerMetrics = new BufferedWriter(new FileWriter(queryfile));
        writerMetrics.write("Query \t Precision \t Recall \t F-Measure \t Average Precision \t NDCG \t Latency");
        writerMetrics.newLine();
        writerMetrics.write("# \t@10  @20  @50\t@10  @20  @50\t@10  @20  @50\t@10  @20  @50\t@10  @20  @50");
        writerMetrics.flush();
    }

    public void readFilterQueryfile(String filterQuery) throws FileNotFoundException {
        File fileQuery = new File(filterQuery);
        Scanner myReader = new Scanner(fileQuery);
        int queryID;
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] queryData = data.split("\\s+");
            queryID = Integer.parseInt(queryData[0]);
            if(!query_Filter.containsKey(queryID))
                query_Filter.put(queryID, new HashMap<>());
            query_Filter.get(queryID).put(queryData[1], Integer.parseInt(queryData[2]));
        }
        myReader.close();
    }


    public void writeMetricsForQueryOnFile(int queryId, int latency) throws IOException {
        writerMetrics.newLine();
        writerMetrics.write(queryId + "\t" + round(precisions.getLastValue(10),2) + " " + round(precisions.getLastValue(20),2) + " " + round(precisions.getLastValue(50),2));
        writerMetrics.write("\t" + round(recalls.getLastValue(10),2) + " " + round(recalls.getLastValue(20),2) + " " + round(recalls.getLastValue(50),2));
        writerMetrics.write("\t" + round(fmeasures.getLastValue(10),2) + " " + round(fmeasures.getLastValue(20),2) + " " + round(fmeasures.getLastValue(50),2));
        writerMetrics.write("\t" + round(averagePrecisions.getLastValue(10),2) + " " + round(averagePrecisions.getLastValue(20),2) + " " + round(averagePrecisions.getLastValue(50),2));
        writerMetrics.write("\t" + round(ndcgs.getLastValue(10),2) + " " + round(ndcgs.getLastValue(20),2) + " " + round(ndcgs.getLastValue(50),2));
        writerMetrics.write("\t" + latency);
        writerMetrics.flush();
    }

    public void calculateMeansAndWriteOnFile(double queryThroughput) throws IOException {
        writerMetrics.newLine();
        writerMetrics.write("Mean\t" + round(precisions.getSumMetric(10)/(double)numQueries,2) + " " + round(precisions.getSumMetric(20)/(double)numQueries,2) + " " + round(precisions.getSumMetric(50)/(double)numQueries,2));
        writerMetrics.write("\t" + round(recalls.getSumMetric(10)/(double)numQueries,2) + " " + round(recalls.getSumMetric(20)/(double)numQueries,2) + " " + round(recalls.getSumMetric(50)/(double)numQueries,2));
        writerMetrics.write("\t" + round(fmeasures.getSumMetric(10)/(double)numQueries,2) + " " + round(fmeasures.getSumMetric(20)/(double)numQueries,2) + " " + round(fmeasures.getSumMetric(50)/(double)numQueries,2));
        writerMetrics.write("\t" + round(averagePrecisions.getSumMetric(10)/(double)numQueries,2) + " " + round(averagePrecisions.getSumMetric(20)/(double)numQueries,2) + " " + round(averagePrecisions.getSumMetric(50)/(double)numQueries,2));
        writerMetrics.write("\t" + round(ndcgs.getSumMetric(10)/(double)numQueries,2) + " " + round(ndcgs.getSumMetric(20)/(double)numQueries,2) + " " + round(ndcgs.getSumMetric(50)/(double)numQueries,2));
        writerMetrics.write("\t" + getLatencyMedian());
        writerMetrics.newLine();
        writerMetrics.write("Query Throughput: " + round(queryThroughput, 2));
        writerMetrics.flush();
        writerMetrics.close();
    }

    public int getLatencyMedian(){
        Collections.sort(latencies);
        int middle;
        if (latencies.size()%2 == 1)
            middle = (latencies.get(latencies.size()/2) + latencies.get(latencies.size()/2 - 1))/2;
        else
            middle = latencies.get(latencies.size() / 2);
        return middle;
    }

    public double calculatePrecision(int tp, int fp)
    {
        int sum = tp + fp; // retrieved -> (Relevant + NonRelevant)
        double prec = 0;
        if (sum!=0) {
            prec = (float)tp/(float)sum;
        }
        return prec;
    }

    public double calculateRecall(int tp, int fn)
    {
        int sum = tp + fn; // retrieved -> (Relevant + NonRelevant)
        double rec = 0;
        if (sum!=0) {
            rec = (float)tp/(float)sum;
        }
        return rec;
    }

    public double calculateFmeasure(double precision, double recall)
    {
        double fmeasure = 0;
        double firstparcel = 2*precision*recall;
        double secondparcel = precision+recall;
        if (secondparcel!=0)
            fmeasure = firstparcel/secondparcel;
        return fmeasure;
    }

    public void calculateMetrics(LinkedHashMap<String, Double> retrieved_Docs, int queryId, int latency) throws IOException {
        int counter = 0;
        LinkedHashMap<String, Double> top = new LinkedHashMap<>();
        for (Map.Entry<String,Double> entry:retrieved_Docs.entrySet()) {
            top.put(entry.getKey(), entry.getValue());
            counter++;
            if (counter == 10)
                calculateTopMetrics(10, top, queryId);
            else if(counter == 20)
                calculateTopMetrics(20, top, queryId);
            else if(counter == 50)
                calculateTopMetrics(50, top, queryId);
        }
        numQueries++;
        latencies.add(latency);
        writeMetricsForQueryOnFile(queryId, latency);
    }

    public void calculateTopMetrics(int numTop, LinkedHashMap<String, Double> retrieved_Docs, int queryId){
        calculateDatatoUseOnPrecisionAndRecall(retrieved_Docs, queryId);
        double precision = calculatePrecision(tp,fp);
        double recall = calculateRecall(tp,fn);
        double fmeasure = calculateFmeasure(precision, recall);
        double ap = calculateAveragePrecision(retrieved_Docs,queryId);
        double ndcg = calculateNDCG(retrieved_Docs,queryId);
        precisions.addNewValue(numTop, precision);
        recalls.addNewValue(numTop, recall);
        fmeasures.addNewValue(numTop, fmeasure);
        averagePrecisions.addNewValue(numTop,ap);
        ndcgs.addNewValue(numTop,ndcg);
    }

    public void calculateDatatoUseOnPrecisionAndRecall(LinkedHashMap<String, Double> retrieved_Docs, int queryId) //dados query.relevance e linkedhashmap da query
    {
        tp = 0; //Retrieved -> Relevant
        fp = 0; //Retrieved -> NonRelevant
        tn = 0; //Not Retrieved -> NonRelevant
        fn = 0; //Not Retrieved -> Relevant

        for(Map.Entry<String, Integer> docs:query_Filter.get(queryId).entrySet()) {
            if (retrieved_Docs.containsKey(docs.getKey())) {
                if (docs.getValue() > 0) //Retrieved and Is Relevant
                    tp++;
                else
                    fp++;
            } else {
                if (docs.getValue() > 0)  //Not Retrieved and Is Relevant
                    fn++;
                else
                    tn++;
            }
        }
    }

    public double calculateAveragePrecision(LinkedHashMap<String, Double> retrieved_Doc,int queryId) {
        int countNdocs = 0; //count documents
        int relevCount = 0; //gives a number of documents relevants
        double sumAveragePrecision = 0.0;
        double ap = 0.0;
        for(Map.Entry<String, Double> doc:retrieved_Doc.entrySet()){
            countNdocs++;
            if(query_Filter.get(queryId).containsKey(doc.getKey())) {
                if (query_Filter.get(queryId).get(doc.getKey()) > 0) {
                    relevCount++;
                    sumAveragePrecision += (double) relevCount / (double) countNdocs;
                }
            }
        }
        if(relevCount > 0)
            ap = sumAveragePrecision/(double)relevCount;
        return ap;
    }

    public ArrayList<Double> calculateIdealDCG(ArrayList<Integer> relevanceOrdered){
        ArrayList<Double> idealDCG = new ArrayList<>();
        int countNdocs = 0;
        double sum = 0;
        for (Integer i:relevanceOrdered) {
            countNdocs++;
            if(countNdocs == 1){
                sum = (double)i;
            } else {
                sum+=(double)i/log2(countNdocs) ;
            }
            idealDCG.add((sum));
        }
        return idealDCG;
    }

    public double calculateNDCG(LinkedHashMap<String, Double> retrieved_Doc, int queryId)
    {
        int countNdocs = 0;
        double sum = 0.0;
        ArrayList<Double> realDCG = new ArrayList<>();
        ArrayList<Double> idealDCG;
        ArrayList<Integer> relevanceOrdered = new ArrayList<>();

        for(Map.Entry<String, Double> doc:retrieved_Doc.entrySet()) {
            countNdocs++;
            if(query_Filter.get(queryId).containsKey(doc.getKey())) {
                relevanceOrdered.add(query_Filter.get(queryId).get(doc.getKey())); //Checks relevance
                if (countNdocs == 1) {
                    sum=(double)query_Filter.get(queryId).get(doc.getKey()); //first doc
                } else {
                    sum += (double) query_Filter.get(queryId).get(doc.getKey())/log2(countNdocs);
                }
            } else{
                relevanceOrdered.add(0); //If not on relevance doc, then it's not relevant
            }
            realDCG.add(sum);
        }
        relevanceOrdered.sort(Collections.reverseOrder());
        idealDCG = calculateIdealDCG(relevanceOrdered);
        sum = 0;
        for(int i = 0; i<realDCG.size();i++){
            if(idealDCG.get(i) > 0)
                sum+= realDCG.get(i) / idealDCG.get(i);
        }
        if(countNdocs > 0)
            return sum/(double)countNdocs;
        return 0.0;
    }

    public double log2(double x) {
        return (Math.log(x) / Math.log(2));
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
