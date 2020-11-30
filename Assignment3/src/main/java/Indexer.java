import com.opencsv.CSVReader;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Indexer {
    private final Tokenizer tokenizer;                    // Class that includes the two tokenizers
    private final HashMap<Integer, String> docIDs;        // Mapping between the generated ID and the document hash
    private final HashMap<String, HashSet<Posting>> index;// Inverted Index
    private final HashMap<String, Double> idfs;      // Mapping of the idf for each term
    private final HashMap<Integer, Integer> dlsBM25; // Mapping the document length for each document (used only to create index BM25)
    private double avdlBM25;                         // Average Document Length (used only to create index BM25)
    private int lastID;                              // Last generated ID

    public Indexer(String stopWordsFilename) throws IOException {
        this.tokenizer = new Tokenizer(stopWordsFilename);
        this.docIDs = new HashMap<>();
        this.index = new HashMap<>();
        this.idfs = new HashMap<>();
        this.dlsBM25 = new HashMap<>();
        this.avdlBM25 = 0.0;
        this.lastID = 0;
    }

    public Indexer() {
        this.tokenizer = null;
        this.docIDs = new HashMap<>();
        this.index = new HashMap<>();
        this.idfs = new HashMap<>();
        this.dlsBM25 = null;
        this.avdlBM25 = 0.0;
    }

    public void loadIndexFromFiles(String indexFilename, String indexDocIDsFilename) throws FileNotFoundException {
        readIndexFromFile(indexFilename);
        readDocIDsFromFile(indexDocIDsFilename);
    }

    public void readIndexFromFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner sc = new Scanner(file);
        String line, term;
        String[] fields, data;
        while(sc.hasNextLine()){
            line = sc.nextLine();
            fields = line.split(";");
            data = fields[0].split(":");
            term = data[0];
            index.put(term, new HashSet<>());
            idfs.put(term, Double.parseDouble(data[1]));
            for (int i = 1; i < fields.length; i++) {
                data = fields[i].split(":");
                index.get(term).add(new Posting(Integer.parseInt(data[0]), Double.parseDouble(data[1])));
            }
        }
        sc.close();
    }

    public void readDocIDsFromFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner sc = new Scanner(file);
        String line;
        String[] fields, data;
        while(sc.hasNextLine()){
            line = sc.nextLine();
            fields = line.split(";");
            data = fields[0].split(":");
            docIDs.put(Integer.parseInt(data[0]), data[1]);
        }
        sc.close();
    }

    public void corpusReader(String corpus, String rankingMethod, String indexFilename, String docsIDsFilename) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(corpus));
        String[] line = reader.readNext(); //Ignores the first line
        //Iterate over the collection of documents (each line is a document)
        while((line = reader.readNext()) != null){
            //Verifies if the abstract is not empty
            if(line[8].length() > 0) {
                Document doc = new Document(line[0], line[3], line[8]);
                addDocToIndex(doc, rankingMethod);
            }
        }
        reader.close();
        writeIndexVSMToFile(indexFilename);
        writeDocIDsToFile(docsIDsFilename);
    }

    public void corpusReader(String corpus, String rankingMethod, String indexFilename, String docsIDsFilename, double k1, double b) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(corpus));
        String[] line = reader.readNext(); //Ignores the first line
        //Iterate over the collection of documents (each line is a document)
        while((line = reader.readNext()) != null){
            //Verifies if the abstract is not empty
            if(line[8].length() > 0) {
                Document doc = new Document(line[0], line[3], line[8]);
                addDocToIndex(doc, rankingMethod);
            }
        }
        reader.close();
        if(rankingMethod.equals("bm25")) {
            calculateBM25Ci(k1, b);
            writeIndexBM25ToFile(indexFilename);
        } else
            writeIndexVSMToFile(indexFilename);
        writeDocIDsToFile(docsIDsFilename);
    }

    public void addDocToIndex(Document doc, String rankingMethod){
        //Create and map the new ID for the document
        addDocID(doc.getId());
        //Get terms of the document using the tokenizer
        //HashSet<String> terms = tokenizer.simpleTokenizer(doc);
        HashMap<String, Integer> terms = tokenizer.improvedTokenizer(doc);
        if(rankingMethod.equals("vsm")) {
            //Calculate Normalized Weights
            HashMap<String, Double> normTerms = calculateNormalizedWeights(terms);
            //Index the terms of the document
            indexTermsVSM(normTerms);
        } else {
            //Index the terms of the document
            indexTermsBM25(terms);
        }
    }

    private HashMap<String, Double> calculateNormalizedWeights(HashMap<String, Integer> terms) {
        HashMap<String, Double> doclength = new HashMap<>();
        double sumDocLength = 0;
        for (Map.Entry<String, Integer> term:terms.entrySet())
        {
            doclength.put(term.getKey(),(1+Math.log(term.getValue())));
            sumDocLength += Math.pow((1+Math.log(term.getValue())),2);
        }

        double squareDocLength = Math.sqrt(sumDocLength);

        for (String s:doclength.keySet())
        {
            double normalizeWeight = round(doclength.get(s)/squareDocLength, 3);
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
    public void indexTermsVSM(HashMap<String, Double> terms){
        for (Map.Entry<String, Double> term:terms.entrySet()) {
            Posting posting = new Posting(lastID, term.getValue());
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

    //Insert terms and postings in index
    public void indexTermsBM25(HashMap<String, Integer> terms){
        int dl = 0;
        for (Map.Entry<String, Integer> term:terms.entrySet()) {
            dl += term.getValue();
            Posting posting = new Posting(lastID, term.getValue());
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
        //Insert Document length
        dlsBM25.put(lastID, dl);
        avdlBM25 += dl;
    }

    public void calculateBM25Ci(double k1, double b){
        avdlBM25 = round(avdlBM25 / (double)dlsBM25.size(),3);
        double score, numerator, denominator, idf;
        for (Map.Entry<String, HashSet<Posting>> entry:index.entrySet()) {
            idf = calculateIdf(entry.getKey());
            for(Posting posting:entry.getValue()) {
                numerator = (k1 + 1) * posting.getTermValue();
                denominator = (k1 * ((1 - b) + b * ((double) dlsBM25.get(posting.getDocID()) / avdlBM25)) + posting.getTermValue());
                score = idf * (numerator / denominator);
                posting.setTermValue(round(score, 3));
            }
            idfs.replace(entry.getKey(), round(idf, 3));
        }
    }

    public void writeIndexVSMToFile(String filename) throws IOException {
        File file = new File(filename);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        double idf;
        for(Map.Entry<String, HashSet<Posting>> entry:index.entrySet()){
            idf = round(calculateIdf(entry.getKey()), 3);
            writer.write(entry.getKey() + ":" + idf + ";" );
            idfs.replace(entry.getKey(), idf);
            for(Posting posting:entry.getValue()){
                writer.write(posting.getDocID() + ":" + posting.getTermValue() + ";");
            }
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public void writeIndexBM25ToFile(String filename) throws IOException {
        File file = new File(filename);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(Map.Entry<String, HashSet<Posting>> entry:index.entrySet()){
            writer.write(entry.getKey() + ":" + idfs.get(entry.getKey()) + ";" );
            for(Posting posting:entry.getValue()){
                writer.write(posting.getDocID() + ":" + posting.getTermValue() + ";");
            }
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public void writeDocIDsToFile(String filename) throws IOException {
        File file = new File(filename);
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

    public HashSet<Posting> getPostingList(String term){
        if(index.containsKey(term))
            return index.get(term);
        else
            return new HashSet<>();
    }

    public String getDocID(int id){
        return docIDs.get(id);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}