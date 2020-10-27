import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Indexer {
    private CorpusReader corpusReader;              // Corpus reader to iterate over the collection
    private Tokenizer tokenizer;                    // Class that includes the two tokenizers
    private HashMap<Integer, String> docIDs;        // Mapping between the generated ID and the document hash
    private HashMap<Term, HashSet<Posting>> index;  // Inverted Index
    private int lastID;                             // Last generated ID

    public Indexer() {
        corpusReader = new CorpusReader();
        tokenizer = new Tokenizer();
        this.docIDs = new HashMap<>();
        this.index = new HashMap<>();
        lastID = 0;
    }

    public void makeIndex(String corpus) {
        int numTermOccurOnDoc;
        /*
        ...... Método do corpus reader
         */
        Document doc = new Document("124", "Este é é um título, de um artigo!", "Agora já é o abstrato de um artigo...");
        addDocID(doc.getId());
        HashSet<String> terms = tokenizer.simpleTokenizer(doc);
        for (String token:terms) {
            //Number of occurrences of the term in the document
            numTermOccurOnDoc = countWordOccurrencesOnString(doc.getTitle().toLowerCase(), token) + countWordOccurrencesOnString(doc.getAbstrct().toLowerCase(), token);
            //Create the new posting
            Posting posting = new Posting(lastID, numTermOccurOnDoc);
            //Checks if the term already exists
            Term term = new Term(token, 1);
            if(index.containsKey(term)){
                //Increment frequency of the existing term, and add new posting to set
                getTermOfIndex(term).incrementFrequency();
                index.get(term).add(posting);
            } else {
                //Insert the new term in the index with the only posting
                index.put(term, new HashSet<>(Arrays.asList(posting)));
            }
        }
    }

    private int nextID(){
        return ++lastID;
    }

    public void addDocID(String id){
        docIDs.put(nextID(), id);
    }

    //Count the number of occurrences of a word in a string
    public int countWordOccurrencesOnString(String str, String word){
        int count = 0;
        List<String> strWords = tokenizer.splitOnWhitespace(str);
        for (String strWord:strWords) {
            if(strWord.equals(word))
                count++;
        }
        return count;
    }

    //Return term of the index
    public Term getTermOfIndex(Term term){
        for (Term t:index.keySet()) {
            if(t.equals(term))
                return t;
        }
        return null;
    }
}
