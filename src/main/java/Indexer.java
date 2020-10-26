import java.util.HashMap;
import java.util.HashSet;

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
    }
}
