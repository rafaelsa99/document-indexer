import java.io.File;
import java.io.IOException;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class RetrievalEngine {

    private static final String results_vsm_filename = "queries/queries_vsm_results.txt";
    private static final String metrics_vsm_filename = "queries/metrics_vsm_results.txt";
    private static final String results_bm25_filename = "queries/queries_bm25_results.txt";
    private static final String metrics_bm25_filename = "queries/metrics_bm25_results.txt";
    private static final String index_vsm_filename = "indexFiles/index_vsm.txt";
    private static final String index_docIDs_vsm_filename = "indexFiles/index_doc_ids_vsm.txt";
    private static final String index_bm25_filename = "indexFiles/index_bm25.txt";
    private static final String index_docIDs_bm25_filename = "indexFiles/index_doc_ids_bm25.txt";

    private static Indexer index;

    public static void main(String[] args) {
        if (((args.length) != 5 && (args.length) != 4) || !(args[3].toLowerCase().equals("vsm") || args[3].toLowerCase().equals("bm25"))) {
            System.out.println("Error! Parameters: stopWordsList queriesFilename queriesRelevanceFilename rankingMethod[\"vsm\" OR \"bm25\"] (Optional: corpusFilename)");
            return;
        }
        try {
            String rankingMethod = args[3].toLowerCase();
            if(((args.length) == 4) && !indexExists(rankingMethod)){ //No index and corpus
                System.out.println("Error! There is no index already created, and no corpus was indicated to create the index.");
                System.out.println("Parameters: stopWordsList queriesFilename queriesRelevanceFilename rankingMethod[\"vsm\" OR \"bm25\"] (Optional: corpusFilename)");
                return;
            }

            if(!indexExists(rankingMethod)) {
                System.out.println("Indexing corpus...");
                createIndex(args[0], args[4], rankingMethod);
            }
            else {
                System.out.println("Loading index...");
                loadIndex(rankingMethod);
            }

            if(rankingMethod.equals("vsm")){
                Query query = new Query(args[0], index, args[2], metrics_vsm_filename);
                query.readQueryFile(args[1], results_vsm_filename);
                System.out.println("Queries results saved on file \"" + results_vsm_filename + "\"");
                System.out.println("Metrics saved on file \"" + metrics_vsm_filename + "\"");
            } else {
                Query query = new Query(args[0], index, args[2], metrics_bm25_filename);
                query.readQueryFile(args[1], results_bm25_filename);
                System.out.println("Queries results saved on file \"" + results_bm25_filename + "\"");
                System.out.println("Metrics saved on file \"" + metrics_bm25_filename + "\"");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean indexExists(String rankingMethod){
        File indexFile, indexDocsFile;
        if(rankingMethod.equals("vsm")) {
            indexFile = new File(index_vsm_filename);
            indexDocsFile = new File(index_docIDs_vsm_filename);
        } else {
            indexFile = new File(index_bm25_filename);
            indexDocsFile = new File(index_docIDs_bm25_filename);
        }
        if (indexFile.exists() && indexFile.isFile() && indexDocsFile.exists() && indexDocsFile.isFile())
            return true;
        else
            return false;
    }

    public static void createIndex(String stopWordsList, String corpusFile, String rankingMethod) throws IOException {
        index = new Indexer(stopWordsList);
        long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        if(rankingMethod.equals("vsm")) //Vector Space Model
            index.corpusReader(corpusFile, rankingMethod, index_vsm_filename, index_docIDs_vsm_filename);  //Entry point
        else //BM25
            index.corpusReader(corpusFile, rankingMethod, index_bm25_filename, index_docIDs_bm25_filename);  //Entry point
        long endTime = System.nanoTime();
        long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if(rankingMethod.equals("vsm")) //Vector Space Model
            System.out.println("Index saved on files \"" + index_vsm_filename + "\" and \"" + index_docIDs_vsm_filename + "\"");
        else //BM25
            System.out.println("Index saved on files \"" + index_bm25_filename + "\" and \"" + index_docIDs_bm25_filename + "\"");
        //Calculate indexing time
        System.out.println("Indexing Time: " + (endTime - startTime) / 1000000000 + " seconds");
        //Calculate Memory Usage
        System.out.println("Memory used for indexing (roughly): " + (usedMemoryAfter-usedMemoryBefore)/(1024*1024) + " MB");
        //Vocabulary Size
        System.out.println("Vocabulary Size: " + index.getVocabularySize() + " terms");
    }

    public static void loadIndex(String rankingMethod) throws IOException {
        index = new Indexer();
        long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        if(rankingMethod.equals("vsm")) //Vector Space Model
            index.loadIndexFromFiles(rankingMethod, index_vsm_filename, index_docIDs_vsm_filename);  //Entry point
        else //BM25
            index.loadIndexFromFiles(rankingMethod, index_bm25_filename, index_docIDs_bm25_filename);  //Entry point
        long endTime = System.nanoTime();
        long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if(rankingMethod.equals("vsm")) //Vector Space Model
            System.out.println("Index loaded from files \"" + index_vsm_filename + "\" and \"" + index_docIDs_vsm_filename + "\"");
        else //BM25
            System.out.println("Index loaded from files \"" + index_bm25_filename + "\" and \"" + index_docIDs_bm25_filename + "\"");
        //Calculate indexing time
        System.out.println("Index loading time: " + (endTime - startTime) / 1000000000 + " seconds");
        //Calculate Memory Usage
        System.out.println("Memory used to load index (roughly): " + (usedMemoryAfter-usedMemoryBefore)/(1024*1024) + " MB");
        //Vocabulary Size
        System.out.println("Vocabulary Size: " + index.getVocabularySize() + " terms");
    }
}
