import java.lang.reflect.Array;
import java.util.*;

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
        /*
        ...... Método do corpus reader
         */
        Document doc = new Document("124", "Este é é um título, de um artigo!", "Agora já é o abstrato de um artigo...");
        Document doc2 = new Document("125", "Este é é um adadad, de um artigo!", "Agora já é o abstrato de um artigo...");
        Document doc3 = new Document("126", "Este é é um títuadaddsdlo, de um artigo!", "Agora já é o abstrato de um artigo...");


        //Create and map the new ID for the document
        addDocID(doc.getId());
        addDocID(doc2.getId());
        addDocID(doc3.getId());
        //Tokenizer
        HashSet<String> terms = tokenizer.simpleTokenizer(doc);
        HashSet<String> terms2 = tokenizer.simpleTokenizer(doc2);
        HashSet<String> terms3 = tokenizer.simpleTokenizer(doc3);
        //Indexer
        indexTerms(terms, doc);
        indexTerms(terms2, doc2);
        indexTerms(terms3, doc3);
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

    //Insert terms and postings in index
    public void indexTerms(HashSet<String> terms, Document doc){
        int numTermOccurOnDoc;
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

    public int getVocabularySize(){
        return index.keySet().size();
    }

    //Ten terms with highest document frequency
    public ArrayList<Term> getTop10Terms(){
        ArrayList<Term> orderedTerms = new ArrayList<>(index.keySet());
        Collections.sort(orderedTerms, new Comparator<Term>() {
            @Override
            public int compare(Term t1, Term t2) {
                return Integer.compare(t2.getFrequency(), t1.getFrequency());
            }
        });
        ArrayList<Term> topTen = new ArrayList<>();
        for (int i = 0; i < 10 && i < orderedTerms.size(); i++)
            topTen.add(orderedTerms.get(i));
        return topTen;
    }

    //Tem terms with document frequency = 1 and ordered alphabetically
    public ArrayList<Term> getTop10TermsDocFreqOne(){
        ArrayList<Term> orderedTerms = new ArrayList<>(index.keySet());
        Collections.sort(orderedTerms);
        ArrayList<Term> topTen = new ArrayList<>();
        for (int i = 0; topTen.size() < 10 && i < orderedTerms.size(); i++)
            if(orderedTerms.get(i).getFrequency() == 1)
                topTen.add(orderedTerms.get(i));
        return topTen;
    }
}
