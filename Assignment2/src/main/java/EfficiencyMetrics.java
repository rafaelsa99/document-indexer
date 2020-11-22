import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class EfficiencyMetrics {

    HashMap<Integer, HashMap<String, Integer>> query_Filter; //<QueryID, HashMap<Cord_UI, Relevance>>

    HashMap<Integer, ArrayList<Double>> precisions;
    HashMap<Integer, ArrayList<Double>> recalls;
    HashMap<Integer, ArrayList<Double>> fmeasures;

    int tp; //Retrieved -> Relevant
    int fp; //Retrieved -> NonRelevant
    int tn; //Not Retrieved -> NonRelevant
    int fn; //Not Retrieved -> Relevant
    int countRelevants;

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

    public EfficiencyMetrics(String filename) throws FileNotFoundException {
        query_Filter = new HashMap<>();
        readFilterQueryfile(filename);
        this.fmeasures = new HashMap<>();
        this.precisions = new HashMap<>();
        this.recalls = new HashMap<>();
        initializeMetricCounters();
    }

    private void initializeMetricCounters() {
        this.precisions.put(10, new ArrayList<>());
        this.precisions.put(20, new ArrayList<>());
        this.precisions.put(50, new ArrayList<>());
        this.fmeasures.put(10, new ArrayList<>());
        this.fmeasures.put(20, new ArrayList<>());
        this.fmeasures.put(50, new ArrayList<>());
        this.recalls.put(10, new ArrayList<>());
        this.recalls.put(20, new ArrayList<>());
        this.recalls.put(50, new ArrayList<>());
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


    public void writefile()
    {

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

    public void calculateMetrics(LinkedHashMap<String, Double> retrieved_Docs, int queryId){
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
        //Escrever para o ficheiro a linha com as metricas da query
    }

    public void calculateTopMetrics(int numTop, LinkedHashMap<String, Double> retrieved_Docs, int queryId){
        calculateDatatoUseOnPrecisionAndRecall(retrieved_Docs, queryId);
        double precision = calculatePrecision(tp,fp);
        double recall = calculateRecall(tp,fn);
        precisions.get(numTop).add(precision);
        recalls.get(numTop).add(recall);
        fmeasures.get(numTop).add(calculateFmeasure(precision, recall));
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

}
