import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */


public class EfficiencyMetrics {

    HashMap<Integer, HashMap<String, Integer>> query_Filter; //<QueryID, HashMap<Cord_UI, Relevance>>

    Metric precisions;
    Metric recalls;
    Metric fmeasures;
    int numQueries;

    int tp; //Retrieved -> Relevant
    int fp; //Retrieved -> NonRelevant
    int tn; //Not Retrieved -> NonRelevant
    int fn; //Not Retrieved -> Relevant
    int countRelevants;

    BufferedWriter writerMetrics;

    //a) Mean Precision
    //b) Mean Recall
    //c) Mean F-measure
    //d) Mean Average Precision (MAP) -> média de todas as query dos valores da precision com base nos documentos relevantes (top 1 prec 0.12 para a query 1, top 1 prec 0.82 para a query 2. map = 0.12+0.82/2  )
    //e) Mean Normalized Discounted Cumulative Gain (NDCG)
    //f) Query throughput
    //g) Median query latency

    /*
    	Precision	Recall	F-measure	Average Precision	NDCG	Latency
Query #	@10	@20	@50	@10	@20	@50	@10	@20	@50	@10	@20	@50	@10	@20	@50
1
2
3
4
...
49
50
Mean

// doc
O ficheiro queries.relevance.txt tem o seguinte formato: query_id, cord_ui, relevance
Para cada query (1..50) existe uma lista de documentos e respetiva relevância: 0 (não relevante), 1 (pouco relevante), 2 (relevante).
     */

    public EfficiencyMetrics(String filename) throws IOException {
        query_Filter = new HashMap<>();
        readFilterQueryfile(filename);
        this.fmeasures = new Metric();
        this.precisions = new Metric();
        this.recalls = new Metric();
        this.numQueries = 0;
        initializeMetricsFile();
    }

    private void initializeMetricsFile() throws IOException {
        final String filePath = "queries/queryMetricsResults.txt";
        File queryfile = new File(filePath);
        writerMetrics = new BufferedWriter(new FileWriter(queryfile));
        writerMetrics.write("Query \t Precision \t Recall \t F-Measure \t Average Precision \t NDCG \t Latency");
        writerMetrics.newLine();
        writerMetrics.write("# \t@10  @20  @50\t@10  @20  @50\t@10  @20  @50\t@10  @20  @50\t@10  @20  @50");
        writerMetrics.flush();
    }

    public void readFilterQueryfile(String filterQuery) throws FileNotFoundException {
        //ler ficheiro queries.relevance.txt
        //mapear para uma estrutura
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


    public void writeMetricsForQueryOnFile(int queryId) throws IOException {
        writerMetrics.newLine();
        writerMetrics.write(queryId + "\t" + round(precisions.getLastValue(10),2) + " " + round(precisions.getLastValue(20),2) + " " + round(precisions.getLastValue(50),2));
        writerMetrics.write("\t" + round(recalls.getLastValue(10),2) + " " + round(recalls.getLastValue(20),2) + " " + round(recalls.getLastValue(50),2));
        writerMetrics.write("\t" + round(fmeasures.getLastValue(10),2) + " " + round(fmeasures.getLastValue(20),2) + " " + round(fmeasures.getLastValue(50),2));
        writerMetrics.flush();
    }

    public void calculateMeansAndWriteOnFile() throws IOException {
        writerMetrics.newLine();
        writerMetrics.write("Mean\t" + round(precisions.getSumMetric(10)/(double)numQueries,2) + " " + round(precisions.getSumMetric(20)/(double)numQueries,2) + " " + round(precisions.getSumMetric(50)/(double)numQueries,2));
        writerMetrics.write(" " + round(recalls.getSumMetric(10)/(double)numQueries,2) + " " + round(recalls.getSumMetric(20)/(double)numQueries,2) + " " + round(recalls.getSumMetric(50)/(double)numQueries,2));
        writerMetrics.write(" " + round(fmeasures.getSumMetric(10)/(double)numQueries,2) + " " + round(fmeasures.getSumMetric(20)/(double)numQueries,2) + " " + round(fmeasures.getSumMetric(50)/(double)numQueries,2));
        writerMetrics.flush();
        writerMetrics.close();
    }

    public double calculatePrecision(int tp, int fp)
    {
        int sum = tp + fp; // retrieved -> (Relevant + NonRelevant)
        double prec = 0;
        if (sum!=0) {
            prec = (float)tp/(float)sum;
            System.out.println("Precision:" + prec + " tp:" + tp + " sum:" + sum);
        }
        return prec;
    }

    public double calculateRecall(int tp, int fn)
    {
        int sum = tp + fn; // retrieved -> (Relevant + NonRelevant)
        double rec = 0;
        if (sum!=0) {
            rec = (float)tp/(float)sum;
            System.out.println("Recall:" + rec);
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
        System.out.println("F-measure:" + fmeasure + " first:" + firstparcel + " second:" + secondparcel);
        return fmeasure;
    }

    public void calculateMetrics(LinkedHashMap<String, Double> retrieved_Docs, int queryId) throws IOException {
        ArrayList<LinkedHashMap<String, Double>> tops = new ArrayList<>();
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
        writeMetricsForQueryOnFile(queryId);
    }

    public void calculateTopMetrics(int numTop, LinkedHashMap<String, Double> retrieved_Docs, int queryId){
        calculateDatatoUseOnPrecisionAndRecall(retrieved_Docs, queryId);
        double precision = calculatePrecision(tp,fp);
        double recall = calculateRecall(tp,fn);
        double fmeasure = calculateFmeasure(precision, recall);
        precisions.addNewValue(numTop, precision);
        recalls.addNewValue(numTop, recall);
        fmeasures.addNewValue(numTop, fmeasure);
    }

    public void calculateDatatoUseOnPrecisionAndRecall(LinkedHashMap<String, Double> retrieved_Docs, int queryId) //dados query.relevance e linkedhashmap da query
    {
        System.out.println("----------------------");
        System.out.println("Query ID: " + queryId + " Top: " + retrieved_Docs.size());
        System.out.println("----------------------");
        tp = 0; //Retrieved -> Relevant
        fp = 0; //Retrieved -> NonRelevant
        tn = 0; //Not Retrieved -> NonRelevant
        fn = 0; //Not Retrieved -> Relevant
        countRelevants = 0;

        for(Map.Entry<String, Integer> docs:query_Filter.get(queryId).entrySet()) {
            if (retrieved_Docs.containsKey(docs.getKey())) {
                if (docs.getValue() > 0) //Retrieved and Is Relevant
                    tp++;
                else
                    fp++;
                countRelevants++;
            } else {
                if (docs.getValue() > 0)  //Not Retrieved and Is Relevant
                    fn++;
                else
                    tn++;
            }
        }
    }

    public void MAP(HashMap<String, Double> prec)
    {
        HashMap<String, Double> Maprecision = new HashMap<>();
        String aux;
        double sumPrec = 0;
        for (String p:prec.keySet())
        {
            //verificacion if query is the same

                sumPrec += prec.get(p);
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
