import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Indexer {
    public static final int MAX_USAGE = 75; // Max percentage of allocated memory while indexing
    public static final int MAX_USED_MEMORY = 90; // Max percentage of used memory
    public static final int MAX_SUBINDEX_SIZE = 5; // Max size of subindex file (in MB)

    private final Tokenizer tokenizer;                    // Class that includes the two tokenizers
    private HashSet<Subindex> subindexes;           // List of subindexes
    private HashMap<Integer, String> docIDs;        // Mapping between the generated ID and the document hash
    private HashMap<String, HashSet<Posting>> index;// Inverted Index
    private HashMap<String, Double> idfs;      // Mapping of the idf for each term
    private HashMap<Integer, Integer> dlsBM25; // Mapping the document length for each document (used only to create index BM25)
    private double avdlBM25;                         // Average Document Length (used only to create index BM25)
    private int lastID;                              // Last generated ID
    private static int blockCounter = 0;                // Number of written blocks while creating the index

    public Indexer(String stopWordsFilename) throws IOException {
        this.tokenizer = new Tokenizer(stopWordsFilename);
        this.subindexes = new HashSet<>();
        this.docIDs = new HashMap<>();
        this.index = new HashMap<>();
        this.idfs = new HashMap<>();
        this.dlsBM25 = new HashMap<>();
        this.avdlBM25 = 0.0;
        this.lastID = 0;
    }

    public Indexer() {
        this.tokenizer = null;
        this.subindexes = new HashSet<>();
        this.docIDs = new HashMap<>();
        this.index = new HashMap<>();
        this.idfs = new HashMap<>();
        this.dlsBM25 = null;
        this.avdlBM25 = 0.0;
    }

    public void loadIndexFromFiles(String indexFilename, String indexDocIDsFilename) throws FileNotFoundException {
        readIndexFromFile(indexFilename);
        loadTermsFromSubindexes();
        readDocIDsFromFile(indexDocIDsFilename);
    }

    public void loadTermsFromSubindexes() throws FileNotFoundException {
        for(Subindex subindex:subindexes){
            readTermsFromSubindexFile(subindex.getFilename());
        }
    }

    public void readIndexFromFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner sc = new Scanner(file);
        String line;
        String[] fields;
        while(sc.hasNextLine()){
            line = sc.nextLine();
            fields = line.split(",");
            Subindex subindex = new Subindex(String.valueOf(fields[0].charAt(0)), String.valueOf(fields[1].charAt(0)), fields[2]);
            subindexes.add(subindex);
        }
        sc.close();
    }

    public void readTermsFromSubindexFile(String filename) throws FileNotFoundException {
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
        }
        sc.close();
    }

    public void readPostingListFromFile(String filename, String term) throws FileNotFoundException {
        File file = new File(filename);
        Scanner sc = new Scanner(file);
        String line;
        String[] fields, data, pos;
        while(sc.hasNextLine()){
            line = sc.nextLine();
            fields = line.split(";");
            data = fields[0].split(":");
            if(data[0].equals(term)) {
                for (int i = 1; i < fields.length; i++) {
                    data = fields[i].split(":");
                    List<Integer> positions = new ArrayList<>();
                    pos = data[2].split(",");
                    for (String p : pos) {
                        positions.add(Integer.parseInt(p));
                    }
                    index.get(term).add(new Posting(Integer.parseInt(data[0]), Double.parseDouble(data[1]), positions));
                }
                break;
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

    public void corpusReader(String corpus, String rankingMethod, String indexFilename, String docsIDsFilename, double ... bm25Parameters) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(corpus));
        String line = reader.readLine(); //Ignores the first line
        //Iterate over the collection of documents (each line is a document)
        while((line = reader.readLine()) != null){
            String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            //Verifies if the abstract is not empty
            if(data.length > 8) {
                if (data[8].length() > 0) {
                    Document doc = new Document(data[0], data[3], data[8]);
                    addDocToIndex(doc, rankingMethod);
                }
            }
            if(getPercentageAllocatedMemory() > MAX_USAGE){
                writeBlockToFile();
                this.index.clear();
                System.gc();
            }
        }
        reader.close();
        writeBlockToFile();
        this.index.clear();
        mergeAllBlocks(indexFilename, rankingMethod, bm25Parameters[0], bm25Parameters[1]);
        writeDocIDsToFile(docsIDsFilename);
        this.idfs.clear();
        this.docIDs.clear();
        System.gc();
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

    public HashSet<Posting> calculateBM25Ci(double k1, double b, String term, HashSet<Posting> postings){
        double score, numerator, denominator, idf;
        idf = calculateIdf(term);
        for(Posting posting:postings) {
            numerator = (k1 + 1) * posting.getTermValue();
            denominator = (k1 * ((1 - b) + b * ((double) dlsBM25.get(posting.getDocID()) / avdlBM25)) + posting.getTermValue());
            score = idf * (numerator / denominator);
            posting.setTermValue(round(score, 3));
        }
        idfs.replace(term, round(idf, 3));
        return postings;
    }

    public void writeBlockToFile() throws IOException {
        TreeMap<String, HashSet<Posting>> orderedIndex = new TreeMap<>(index);
        blockCounter++;
        String filename = "indexFiles/index_block_" + blockCounter + ".txt";
        File file = new File(filename);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(Map.Entry<String, HashSet<Posting>> entry:orderedIndex.entrySet()){
            writer.write(entry.getKey() + ":" + idfs.get(entry.getKey()) + ";" );
            writePostingList(writer, entry.getValue());
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public void writePostingList(BufferedWriter writer, HashSet<Posting> postings) throws IOException {
        for(Posting posting:postings){
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
    }

    public void mergeAllBlocks(String fileName, String rankingMethod, double ... bm25Parameters) throws IOException {
        Scanner[] scanners = new Scanner[blockCounter];
        String[] currentLines = new String[blockCounter];
        boolean[] EOFs = new boolean[blockCounter];
        String[] fields, data, pos;
        int countEOFs = 0, countSubindex = 1;
        String minTerm = "", subindex_filename = "indexFiles/subindex_" + rankingMethod + "_" + countSubindex + ".txt";
        char currentLetter = 'a', lastLetterFile = 'a', startLetter = 'a';
        boolean newFile = false;
        File file = new File(subindex_filename);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        File fileMainFile = new File(fileName);
        BufferedWriter writerMainFile = new BufferedWriter(new FileWriter(fileMainFile));
        avdlBM25 = round(avdlBM25 / (double)dlsBM25.size(),3);
        double sumDFs;
        for (int i = 0, j = 1; i < blockCounter ; i++, j++) {
            String filename = "indexFiles/index_block_" + j + ".txt";
            scanners[i] = new Scanner(new File(filename));
            EOFs[i] = false;
            if (scanners[i].hasNextLine())
                currentLines[i] = scanners[i].nextLine();
            else {
                EOFs[i] = true;
                countEOFs++;
            }
        }
        while (countEOFs < blockCounter){
            if((file.length() / (1024 * 1024)) >= MAX_SUBINDEX_SIZE && !newFile) {
                newFile = true;
                lastLetterFile = currentLetter;
            }
            for (int i = 0; i < blockCounter; i++) {
                if(!EOFs[i]) {
                    minTerm = getTermOfFileLine(currentLines[i]);
                    currentLetter = minTerm.charAt(0);
                    break;
                }
            }
            for (int i = 1; i < blockCounter; i++) {
                if(!EOFs[i]) {
                    if (getTermOfFileLine(currentLines[i]).compareTo(minTerm) < 0) {
                        minTerm = getTermOfFileLine(currentLines[i]);
                        currentLetter = minTerm.charAt(0);
                    }
                }
            }
            if(lastLetterFile != currentLetter && newFile){
                newFile = false;
                countSubindex++;
                writer.flush();
                writer.close();
                writerMainFile.write(startLetter + "," + lastLetterFile + "," + subindex_filename);
                writerMainFile.newLine();
                startLetter = currentLetter;
                subindex_filename = "indexFiles/subindex_" + rankingMethod + "_" + countSubindex + ".txt";
                file = new File(subindex_filename);
                writer = new BufferedWriter(new FileWriter(file));
            }
            HashSet<Posting> postings = new HashSet<>();
            writer.write(minTerm + ":");
            sumDFs = 0.0;
            for (int i = 0; i < blockCounter; i++) {
                if(!EOFs[i]){
                    if (getTermOfFileLine(currentLines[i]).compareTo(minTerm) == 0) {
                        fields = currentLines[i].split(";");
                        data = fields[0].split(":");
                        sumDFs += Double.parseDouble(data[1]);
                        for (int j = 1; j < fields.length; j++) {
                            data = fields[j].split(":");
                            List<Integer> positions = new ArrayList<>();
                            pos = data[2].split(",");
                            for (String p : pos) {
                                positions.add(Integer.parseInt(p));
                            }
                            postings.add(new Posting(Integer.parseInt(data[0]), Double.parseDouble(data[1]), positions));
                        }
                        if (scanners[i].hasNextLine()) {
                            currentLines[i] = scanners[i].nextLine();
                        }
                        else {
                            EOFs[i] = true;
                            countEOFs++;
                        }
                    }
                }
            }
            idfs.putIfAbsent(minTerm, sumDFs);
            if(rankingMethod.equals("bm25")) {
                postings = calculateBM25Ci(bm25Parameters[0], bm25Parameters[1], minTerm, postings);
            } else {
                double idf = round(calculateIdf(minTerm), 3);
                idfs.replace(minTerm, idf);
            }
            writer.write(idfs.get(minTerm) + ";");
            writePostingList(writer, postings);
            writer.newLine();
        }
        writer.flush();
        writer.close();
        writerMainFile.write(startLetter + "," + currentLetter + "," + subindex_filename);
        writerMainFile.newLine();
        writerMainFile.flush();
        writerMainFile.close();
        for (int i = 0, j = 1; i < blockCounter; i++, j++) {
            scanners[i].close();
            String filename = "indexFiles/index_block_" + j + ".txt";
            File fileToDelete = new File(filename);
            fileToDelete.delete();
        }
    }

    public String getTermOfFileLine(String line){
        String[] fields, data;
        fields = line.split(";");
        data = fields[0].split(":");
        return data[0];
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
        if(index.containsKey(term)) {
            if(index.get(term).size() > 0)
                return index.get(term);
            else{
                putPostingListInMemory(term);
                return index.get(term);
            }
        }else
            return new HashSet<>();
    }

    public void putPostingListInMemory(String term) {
        while (getPercentageUsedMemory() > MAX_USED_MEMORY){
            System.gc();
            for (String t:index.keySet()) {
                index.replace(t, new HashSet<>());
                System.out.println("Deleting Posting List From Memory...");
                break;
            }
        }
        String filename = getSubindexByLetter(String.valueOf(term.charAt(0)));
        if(filename != null){
            try {
                readPostingListFromFile(filename, term);
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public String getSubindexByLetter(String letter) {
        for (Subindex subindex:subindexes){
            if(letter.compareTo(subindex.getStartingLetter()) >= 0 && letter.compareTo(subindex.getEndingLetter()) <= 0){
                return subindex.getFilename();
            }
        }
        return null;
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

    public double getPercentageAllocatedMemory(){
        long MAX_RAM = Runtime.getRuntime().maxMemory();
        long TOTAL_RAM = Runtime.getRuntime().totalMemory();
        return ((TOTAL_RAM * 1.0) / MAX_RAM) * 100;
    }

    public double getPercentageUsedMemory(){
        long FREE_RAM = Runtime.getRuntime().freeMemory();
        long TOTAL_RAM = Runtime.getRuntime().totalMemory();
        long USED_RAM = TOTAL_RAM - FREE_RAM;
        return ((USED_RAM * 1.0) / TOTAL_RAM) * 100;
    }
}