import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Indexer {
    private final Tokenizer tokenizer;                    // Class that includes the two tokenizers
    private final HashMap<Integer, String> docIDs;        // Mapping between the generated ID and the document hash
    private final HashMap<String, HashSet<PostingTf>> index;// Inverted Index
    private final HashMap<String, Double> idfs;       // Mapping of the idf for each term
    private int lastID;                             // Last generated ID

    public Indexer(String stopWordsFilename) throws IOException {
        this.tokenizer = new Tokenizer(stopWordsFilename);
        this.docIDs = new HashMap<>();
        this.index = new HashMap<>();
        this.idfs = new HashMap<>();
        this.lastID = 0;
    }

    public void corpusReader(String corpus) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(corpus));
        String[] line; //Ignores the first line
        //Iterate over the collection of documents (each line is a document)
        while((line = reader.readNext()) != null){
            //Verifies if the abstract is not empty
            if(line[8].length() > 0) {
                Document doc = new Document(line[0], line[3], line[8]);
                addDocToIndex(doc);
            }
        }
        reader.close();
        writeIndexToFile();
        writeDocIDsToFile();
    }

    public void addDocToIndex(Document doc){
        //Create and map the new ID for the document
        addDocID(doc.getId());
        //Get terms of the document using the tokenizer
        //HashSet<String> terms = tokenizer.simpleTokenizer(doc);
        HashMap<String, Integer> terms = tokenizer.improvedTokenizer(doc);
        //Calculate Normalized Weights
        HashMap<String, Double> normTerms = calculateNormalizedWeights(terms);
        //Index the terms of the document
        indexTerms(normTerms);
    }

    private HashMap<String, Double> calculateNormalizedWeights(HashMap<String, Integer> terms) {
        HashMap<String, Double> doclength = new HashMap<>();
        double sumDocLength = 0;
        for (Map.Entry<String, Integer> term:terms.entrySet())
        {
            doclength.put(term.getKey(),(Math.pow(1+Math.log(term.getValue()),2)));
            sumDocLength += Math.pow((1+Math.log(term.getValue())),2);
        }

        double squareDocLength = Math.sqrt(sumDocLength);

        for (String s:doclength.keySet())
        {
            double normalizeWeight = doclength.get(s)/squareDocLength;
            doclength.replace(s,normalizeWeight);
        }
        return doclength;
    }

    private int nextID(){
        return ++lastID;
    }

    public void addDocID(String id){
        docIDs.put(nextID(), id);
    }

    //Insert terms and postings in index
    public void indexTerms(HashMap<String, Double> terms){
        for (Map.Entry<String, Double> term:terms.entrySet()) {
            PostingTf posting = new PostingTf(lastID, term.getValue());
            //Checks if the term already exists
            if(index.containsKey(term.getKey())){
                //Increment frequency of the existing term, and add new posting to set
                index.get(term.getKey()).add(posting);
                idfs.replace(term.getKey(), (idfs.get(term.getKey()) + 1.0));
            } else {
                //Insert the new term in the index with the only posting
                index.put(term.getKey(), new HashSet<>(Arrays.asList(posting)));
                idfs.put(term.getKey(), 1.0);
            }
        }
    }

    public void writeIndexToFile() throws IOException {
        final String filePath = "indexFiles/tf_idf_index.txt";
        File file = new File(filePath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        double idf;
        for(Map.Entry<String, HashSet<PostingTf>> entry:index.entrySet()){
            idf = calculateIdf(entry.getKey());
            writer.write(entry.getKey() + ":" + idf + ";" );
            idfs.replace(entry.getKey(), idf);
            for(PostingTf posting:entry.getValue()){
                writer.write(posting.getDocID() + ":" + posting.getTermFreq() + ";");
            }
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public void writeDocIDsToFile() throws IOException {
        final String filePath = "indexFiles/tf_idf_index_doc_ids.txt";
        File file = new File(filePath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(Map.Entry<Integer, String> entry:docIDs.entrySet()){
            writer.write(entry.getKey() + ":" + entry.getValue() + ";" );
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public int getVocabularySize(){
        return index.keySet().size();
    }

    public double calculateIdf(String term){
        int totalDocs = docIDs.size(); // Total of Documents
        return Math.log10((double)totalDocs / idfs.get(term));
    }

    public double getIdf(String term) {
        if(index.containsKey(term))
            return idfs.get(term);
        else
            return 0;
    }

    public HashSet<PostingTf> getPostingList(String term){
        if(index.containsKey(term))
            return index.get(term);
        else
            return new HashSet<PostingTf>();
    }

    public String getDocID(int id){
        return docIDs.get(id);
    }

}