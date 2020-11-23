import java.io.*;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */


public class Query {
    Tokenizer tokenizer;
    Indexer index;
    BufferedWriter writerTopDocs;
    LinkedHashMap<String, Double> topDocs; //retrie top x documents to a query
    EfficiencyMetrics efficiencyMetrics;

    public Query(String stopWords, Indexer indexer, String relevanceFilename) throws IOException {
        this.tokenizer = new Tokenizer(stopWords);
        this.index = indexer;
        this.efficiencyMetrics = new EfficiencyMetrics(relevanceFilename);
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

    public void readQueryFile(String file) throws IOException
    {
        final String filePath = "queries/querySortedResults.txt";
        File queryfile = new File(filePath);
        File myObj = new File(file);
        Scanner myReader = new Scanner(myObj);
        HashMap <String, Double> weightQuery;
        HashMap<String, Integer> terms;
        writerTopDocs = new BufferedWriter(new FileWriter(queryfile));
        int idQ = 1;
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            terms = tokenizer.improvedTokenizerforQuery(data);
            weightQuery = getQueryWeights(terms);
            topDocs = getCosineScores(weightQuery, 50);
            writeTopDocs(topDocs, data, idQ);
            efficiencyMetrics.calculateMetrics(topDocs, idQ);
            idQ++;
        }
        efficiencyMetrics.calculateMeansAndWriteOnFile();
        myReader.close();
        writerTopDocs.close();
    }

    public LinkedHashMap<Integer,Double> ordenateHashMap(HashMap<Integer,Double> scores)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Double> > list = new LinkedList<>(scores.entrySet());
        // Sort the list
        list.sort(new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

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
                    scores.replace(posting.getDocID(), scores.get(posting.getDocID()) + (weightQuery.get(q)*posting.getTermWeight()));
                else scores.put(posting.getDocID(),weightQuery.get(q)*posting.getTermWeight());
            }
        }
        int count = 0;
        LinkedHashMap<Integer, Double> orderedScores = ordenateHashMap(scores);
        LinkedHashMap<String, Double> topDocs = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry:orderedScores.entrySet()) {
            topDocs.put(index.getDocID(entry.getKey()), entry.getValue());
            count++;
            if(count == numTopDocs)
                break;
        }
        return topDocs;
    }
}
