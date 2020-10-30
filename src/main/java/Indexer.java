import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Indexer {
    private Tokenizer tokenizer;                    // Class that includes the two tokenizers
    private HashMap<Integer, String> docIDs;        // Mapping between the generated ID and the document hash
    private HashMap<String, HashSet<Integer>> index;// Inverted Index
    private HashMap<String, Integer> docFreq;       // Mapping of the document frequency for each term
    private int lastID;                             // Last generated ID

    public Indexer(String stopWordsFilename) throws IOException {
        this.tokenizer = new Tokenizer(stopWordsFilename);
        this.docIDs = new HashMap<>();
        this.index = new HashMap<>();
        this.docFreq = new HashMap<>();
        this.lastID = 0;
    }

    public void corpusReader(String corpus) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(corpus));
        String[] line;
        //Iterate over the collection of documents (each line is a document)
        while((line = reader.readNext()) != null){
            //Verifies if the abstract is not empty
            if(line[7].length() > 0) {
                Document doc = new Document(line[3], line[2], line[7]);
                addDocToIndex(doc);
            }
        }
        reader.close();
    }

    public void addDocToIndex(Document doc){
        //Create and map the new ID for the document
        addDocID(doc.getId());
        //Get terms of the document using the tokenizer
        //HashSet<String> terms = tokenizer.simpleTokenizer(doc);
        HashSet<String> terms = tokenizer.improvedTokenizer(doc);
        //Index the terms of the document
        indexTerms(terms);
    }

    private int nextID(){
        return ++lastID;
    }

    public void addDocID(String id){
        docIDs.put(nextID(), id);
    }

    //Insert terms and postings in index
    public void indexTerms(HashSet<String> terms){
        for (String term:terms) {
            //Checks if the term already exists
            if(index.containsKey(term)){
                //Increment frequency of the existing term, and add new posting to set
                index.get(term).add(lastID);
                docFreq.replace(term, (docFreq.get(term) + 1));
            } else {
                //Insert the new term in the index with the only posting
                index.put(term, new HashSet<>(Arrays.asList(lastID)));
                docFreq.put(term, 1);
            }
        }
    }

    public int getVocabularySize(){
        return index.keySet().size();
    }

    //Ten terms with highest document frequency
    public ArrayList<String> getTop10Terms(){
        ArrayList<String> topTen = new ArrayList<>();
        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(docFreq.entrySet());
        //Order terms by document frequency
        list.sort(new Comparator<>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for (int i = 0; topTen.size() < 10 && i < list.size(); i++)
                topTen.add(list.get(i).getKey());
        return topTen;
    }

    //Ten terms with document frequency = 1 and ordered alphabetically
    public ArrayList<String> getTop10TermsDocFreqOne(){
        ArrayList<String> orderedTerms = new ArrayList<>(docFreq.keySet());
        //Order terms alphabetically
        Collections.sort(orderedTerms);
        ArrayList<String> topTen = new ArrayList<>();
        for (int i = 0; topTen.size() < 10 && i < orderedTerms.size(); i++)
            if(docFreq.get(orderedTerms.get(i)) == 1)
                topTen.add(orderedTerms.get(i));
        return topTen;
    }

    public int getDocFreq(String term){
        return docFreq.get(term);
    }
}
