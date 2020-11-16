import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Indexer {
    private Tokenizer tokenizer;                    // Class that includes the two tokenizers
    private HashMap<Integer, String> docIDs;        // Mapping between the generated ID and the document hash
    private HashMap<String, HashSet<PostingTf>> index;// Inverted Index
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
        String[] line = reader.readNext(); //Ignores the first line
        //Iterate over the collection of documents (each line is a document)
        while((line = reader.readNext()) != null){
            //Verifies if the abstract is not empty
            if(line[7].length() > 0) {
                Document doc = new Document(line[3], line[2], line[7]);
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
    public void indexTerms(HashMap<String, Integer> terms){
        for (Map.Entry<String, Integer> term:terms.entrySet()) {
            PostingTf posting = new PostingTf(lastID, term.getValue());
            //Checks if the term already exists
            if(index.containsKey(term.getKey())){
                //Increment frequency of the existing term, and add new posting to set
                index.get(term.getKey()).add(posting);
                docFreq.replace(term.getKey(), (docFreq.get(term.getKey()) + 1));
            } else {
                //Insert the new term in the index with the only posting
                index.put(term.getKey(), new HashSet<>(Arrays.asList(posting)));
                docFreq.put(term.getKey(), 1);
            }
        }
    }

    public void writeIndexToFile() throws IOException {
        final String filePath = "indexFiles/tf_idf_index.txt";
        File file = new File(filePath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(Map.Entry<String, HashSet<PostingTf>> entry:index.entrySet()){
            writer.write(entry.getKey() + ":" + getIdf(entry.getKey()) + ";" );
            for(PostingTf posting:entry.getValue()){
                double termWeight = 0;
                //----------------> CALCULAR TERM WEIGHT
                writer.write(posting.getDocID() + ":" + termWeight + ";");
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

    public double getIdf(String term){
        int totalDocs = docIDs.size(); // Total of Documents
        return Math.log10((double)totalDocs / docFreq.get(term));
    }

    public int getDocFreq(String term){
        return docFreq.get(term);
    }

}

