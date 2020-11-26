import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */


public class Query {
    Tokenizer tokenizer;
    Indexer index;
    BufferedWriter writerTopDocs;
    LinkedHashMap<String, Double> topDocs; //top x documents to a query
    Metrics metrics_rel_1_2;
    Metrics metrics_rel_2;
    double totalTimeToProcessQueries; //In seconds


    public Query(String stopWords, Indexer indexer, String relevanceFilename, String metricsRel1Filename, String metricsRel2Filename) throws IOException {
        this.tokenizer = new Tokenizer(stopWords);
        this.index = indexer;
        this.metrics_rel_1_2 = new Metrics(relevanceFilename, metricsRel1Filename, 1);
        this.metrics_rel_2 = new Metrics(relevanceFilename, metricsRel2Filename, 2);
        totalTimeToProcessQueries = 0;
    }

    public HashMap <String, Double> getQueryWeights(HashMap<String, Integer> terms){
        double sumWeightQ = 0;
        double wtdf = 1;
        double idf;
        HashMap <String, Double> weightQuery = new HashMap<>();
        for (String termos : terms.keySet())
        {
            idf = index.getIdf(termos);
            wtdf = ((1+Math.log(terms.get(termos)))*idf);
            sumWeightQ += Math.pow(wtdf,2);
        }
        sumWeightQ = Math.sqrt(sumWeightQ);
        for (String termos : terms.keySet())
        {
            weightQuery.put(termos,(wtdf/sumWeightQ));
        }
        return weightQuery;
    }

    public void writeTopDocs(LinkedHashMap<String, Double> topDocs, String query, int queryID) throws IOException {
        int count = 0;
        writerTopDocs.write("Query ID: " + queryID + " ; Query: " + query);
        writerTopDocs.newLine();
        for (Map.Entry<String, Double> entry: topDocs.entrySet()) {
            count++;
            writerTopDocs.write("Top " + count + " -> Document ID: " + entry.getKey() + " ; Score: " + entry.getValue());
            writerTopDocs.newLine();
        }
        writerTopDocs.flush();
    }

    public void readQueryFileVSM(String queryFilename, String resultsFilename) throws IOException
    {
        File queryfile = new File(resultsFilename);
        File myObj = new File(queryFilename);
        Scanner myReader = new Scanner(myObj);
        HashMap <String, Double> weightQuery;
        HashMap<String, Integer> terms;
        long startTime, endTime;
        int latency;
        writerTopDocs = new BufferedWriter(new FileWriter(queryfile));
        int idQ = 1;
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            startTime = System.nanoTime();
            terms = tokenizer.improvedTokenizerforQuery(data);
            weightQuery = getQueryWeights(terms);
            topDocs = getCosineScores(weightQuery, 50);
            writeTopDocs(topDocs, data, idQ);
            endTime = System.nanoTime();
            latency = (int)((endTime - startTime) / 1000000); //In milliseconds
            metrics_rel_1_2.calculateMetrics(topDocs, idQ, latency);
            metrics_rel_2.calculateMetrics(topDocs, idQ, latency);
            idQ++;
            totalTimeToProcessQueries += (double) latency / 1000.0;
        }
        metrics_rel_1_2.calculateMeansAndWriteOnFile(getQueryThroughput(idQ - 1));
        metrics_rel_2.calculateMeansAndWriteOnFile(getQueryThroughput(idQ - 1));
        myReader.close();
        writerTopDocs.close();
    }

    public void readQueryFileBM25(String queryFilename, String resultsFilename) throws IOException
    {
        File queryfile = new File(resultsFilename);
        File myObj = new File(queryFilename);
        Scanner myReader = new Scanner(myObj);
        List<String> terms;
        long startTime, endTime;
        int latency;
        writerTopDocs = new BufferedWriter(new FileWriter(queryfile));
        int idQ = 1;
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            startTime = System.nanoTime();
            terms = tokenizer.improvedTokenizerforQueryBM25(data);
            topDocs = getRSVBM25(terms, 50);
            writeTopDocs(topDocs, data, idQ);
            endTime = System.nanoTime();
            latency = (int)((endTime - startTime) / 1000000); //In milliseconds
            metrics_rel_1_2.calculateMetrics(topDocs, idQ, latency);
            metrics_rel_2.calculateMetrics(topDocs, idQ, latency);
            idQ++;
            totalTimeToProcessQueries += (double) latency / 1000.0;
        }
        metrics_rel_1_2.calculateMeansAndWriteOnFile(getQueryThroughput(idQ - 1));
        metrics_rel_2.calculateMeansAndWriteOnFile(getQueryThroughput(idQ - 1));
        myReader.close();
        writerTopDocs.close();
    }

    public double getQueryThroughput(int numQueries){
        return (double)numQueries / totalTimeToProcessQueries;
    }

    public LinkedHashMap<Integer,Double> ordenateHashMap(HashMap<Integer,Double> scores)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Double> > list = new LinkedList<>(scores.entrySet());
        // Sort the list
        list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        // put data from sorted list to hashmap
        LinkedHashMap<Integer, Double> temp = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public LinkedHashMap<String, Double> getCosineScores(HashMap<String, Double> weightQuery, int numTopDocs)
    {
        HashMap<Integer,Double> scores = new HashMap<>(); //save scores
        for(String q:weightQuery.keySet()) //iterate over terms on the query
        {
            for(Posting posting:index.getPostingList(q)){
                if(scores.containsKey(posting.getDocID()))
                    scores.replace(posting.getDocID(), scores.get(posting.getDocID()) + (weightQuery.get(q)*posting.getTermValue()));
                else scores.put(posting.getDocID(),weightQuery.get(q)*posting.getTermValue());
            }
        }
        return getTopDocs(scores, numTopDocs);
    }

    public LinkedHashMap<String, Double> getRSVBM25(List<String> termsQuery, int numTopDocs)
    {
        HashMap<Integer,Double> scores = new HashMap<>(); //save scores
        for (String term:termsQuery){
            for(Posting posting:index.getPostingList(term)){
                if(scores.containsKey(posting.getDocID()))
                    scores.replace(posting.getDocID(), scores.get(posting.getDocID()) + posting.getTermValue());
                else scores.put(posting.getDocID(), posting.getTermValue());
            }
        }
        return getTopDocs(scores, numTopDocs);
    }

    public LinkedHashMap<String, Double> getTopDocs(HashMap<Integer,Double> scores, int numTopDocs){
        int count = 0;
        LinkedHashMap<Integer, Double> orderedScores = ordenateHashMap(scores);
        LinkedHashMap<String, Double> topDocs = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry:orderedScores.entrySet()) {
            topDocs.put(index.getDocID(entry.getKey()), round(entry.getValue(), 3));
            count++;
            if(count == numTopDocs)
                break;
        }
        return topDocs;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
