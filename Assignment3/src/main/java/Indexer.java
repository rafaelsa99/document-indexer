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
        String[] fields, data, pos;
        while(sc.hasNextLine()){
            line = sc.nextLine();
            fields = line.split(";");
            data = fields[0].split(":");
            term = data[0];
            index.put(term, new HashSet<>());
            idfs.put(term, Double.parseDouble(data[1]));
            for (int i = 1; i < fields.length; i++) {
                data = fields[i].split(":");
                List<Integer> positions = new ArrayList<>();
                pos = data[2].split(",");
                for(String p:pos) {
                    positions.add(Integer.parseInt(p));
                }
                index.get(term).add(new Posting(Integer.parseInt(data[0]), Double.parseDouble(data[1]), positions));
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
        //HashSet<String> terms = tokenizer.simpleTokenizer(doc.getText());
        List<Term> terms = tokenizer.improvedTokenizer(doc.getText());
        if(rankingMethod.equals("vsm")) {
            //Calculate Normalized Weights
            List<Term> normTerms = calculateNormalizedWeights(terms);
            //Index the terms of the document
            indexTermsVSM(normTerms);
        } else {
            //Index the terms of the document
            indexTermsBM25(terms);
        }
    }

    private List<Term> calculateNormalizedWeights(List<Term> terms) {
        double sumDocLength = 0;
        for (Term term:terms) {
            term.setTermValue(1+Math.log(term.getTermValue()));
            sumDocLength += Math.pow((1+Math.log(term.getTermValue())),2);
        }
        double squareDocLength = Math.sqrt(sumDocLength);
        for (Term term:terms) {
            double normalizeWeight = round(term.getTermValue()/squareDocLength, 3);
            term.setTermValue(normalizeWeight);
        }
        return terms;
    }

    private int nextID(){
        return ++lastID;
    }

    public void addDocID(String id){
        docIDs.put(nextID(), id);
    }

    //Insert terms and postings in index
    public void indexTermsVSM(List<Term> terms){
        for(Term term:terms) {
            Posting posting = new Posting(lastID, term.getTermValue(), term.getPositions());
            //Checks if the term already exists
            if(index.containsKey(term.getTerm())){
                //Increment frequency of the existing term, and add new posting to set
                index.get(term.getTerm()).add(posting);
                idfs.replace(term.getTerm(), (idfs.get(term.getTerm()) + 1.0));
            } else {
                //Insert the new term in the index with the only posting
                index.put(term.getTerm(), new HashSet<>(Arrays.asList(posting)));
                idfs.put(term.getTerm(), 1.0);
            }
        }
    }

    //Insert terms and postings in index
    public void indexTermsBM25(List<Term> terms){
        int dl = 0;
        for (Term term:terms) {
            dl += term.getTermValue();
            Posting posting = new Posting(lastID, term.getTermValue(), term.getPositions());
            //Checks if the term already exists
            if(index.containsKey(term.getTerm())){
                //Increment frequency of the existing term, and add new posting to set
                index.get(term.getTerm()).add(posting);
                idfs.replace(term.getTerm(), (idfs.get(term.getTerm()) + 1.0));

            } else {
                //Insert the new term in the index with the only posting
                index.put(term.getTerm(), new HashSet<>(Arrays.asList(posting)));
                idfs.put(term.getTerm(), 1.0);
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
                writer.write(posting.getDocID() + ":" + posting.getTermValue() + ":");
                boolean isFirst = true;
                for(Integer position: posting.getPositions()){
                    if(!isFirst)
                        writer.write(",");
                    else
                        isFirst = false;
                    String pos = String.valueOf(position);
                    writer.write(pos);
                }
                writer.write(";");
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
                writer.write(posting.getDocID() + ":" + posting.getTermValue() + ":");
                boolean isFirst = true;
                for(Integer position: posting.getPositions()){
                    if(!isFirst)
                        writer.write(",");
                    else
                        isFirst = false;
                    String pos = String.valueOf(position);
                    writer.write(pos);
                }
                writer.write(";");
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