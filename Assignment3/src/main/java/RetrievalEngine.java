import java.io.File;
import java.io.IOException;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class RetrievalEngine {

    private static final String results_vsm_filename = "queries/queries_vsm_results.txt";
    private static final String results_vsm_boost_filename = "queries/queries_vsm_boost_results.txt";
    private static final String metrics_rel_1_2_vsm_filename = "queries/metrics_rel_1_2_vsm_results.txt";
    private static final String metrics_rel_2_vsm_filename = "queries/metrics_rel_2_vsm_results.txt";
    private static final String metrics_rel_1_2_vsm_boost_filename = "queries/metrics_rel_1_2_vsm_boost_results.txt";
    private static final String metrics_rel_2_vsm_boost_filename = "queries/metrics_rel_2_vsm_boost_results.txt";
    private static final String results_bm25_filename = "queries/queries_bm25_results.txt";
    private static final String metrics_rel_1_2_bm25_filename = "queries/metrics_rel_1_2_bm25_results.txt";
    private static final String metrics_rel_2_bm25_filename = "queries/metrics_rel_2_bm25_results.txt";
    private static final String results_bm25_boost_filename = "queries/queries_bm25_boost_results.txt";
    private static final String metrics_rel_1_2_bm25_boost_filename = "queries/metrics_rel_1_2_bm25_boost_results.txt";
    private static final String metrics_rel_2_bm25_boost_filename = "queries/metrics_rel_2_bm25_boost_results.txt";
    private static final String index_vsm_filename = "indexFiles/index_vsm.txt";
    private static final String index_docIDs_vsm_filename = "indexFiles/index_doc_ids_vsm.txt";
    private static final String index_bm25_filename = "indexFiles/index_bm25.txt";
    private static final String index_docIDs_bm25_filename = "indexFiles/index_doc_ids_bm25.txt";
    private static double bm25_k1 = 1.2;
    private static double bm25_b = 0.75;

    private static Indexer index;

    public static void main(String[] args) {
        if (((args.length) != 6 && (args.length) != 5 && (args.length) != 8) || !(args[3].equalsIgnoreCase("vsm") || args[3].equalsIgnoreCase("bm25")) || !(args[4].equalsIgnoreCase("boost") || args[4].equalsIgnoreCase("noBoost"))) {
            System.out.println("Error! Parameters: stopWordsList queriesFilename queriesRelevanceFilename rankingMethod[\"vsm\" OR \"bm25\"] proximityBoost[\"boost\" OR \"noBoost\"] (Optional: corpusFilename k1 b)");
            return;
        }
        try {
            String rankingMethod = args[3].toLowerCase();
            boolean boost = false;
            if(args[4].equalsIgnoreCase("boost"))
                boost = true;
            if(((args.length) == 5) && !indexExists(rankingMethod)){ //No index and corpus
                System.out.println("Error! There is no index already created, and no corpus was indicated to create the index.");
                System.out.println("Parameters: stopWordsList queriesFilename queriesRelevanceFilename rankingMethod[\"vsm\" OR \"bm25\"] proximityBoost[\"boost\" OR \"noBoost\"] (Optional: corpusFilename k1 b)");
                return;
            }

            if(args.length == 8){
                if(Double.parseDouble(args[6]) >= 0)
                    bm25_k1 = Double.parseDouble(args[6]);
                if(Double.parseDouble(args[7]) >= 0 && Double.parseDouble(args[7]) <= 2)
                    bm25_b = Double.parseDouble(args[7]);
            }

            if(!indexExists(rankingMethod)) {
                System.out.println("Indexing corpus...");
                createIndex(args[0], args[5], rankingMethod);
                System.out.println("Loading index...");
            }
            else {
                if(rankingMethod.equals("vsm")) //Vector Space Model
                    System.out.println("Loading index from files \"" + index_vsm_filename + "\" and \"" + index_docIDs_vsm_filename + "\"...");
                else //BM25
                    System.out.println("Loading index from files \"" + index_bm25_filename + "\" and \"" + index_docIDs_bm25_filename + "\"...");
            }
            loadIndex(rankingMethod);

            if(rankingMethod.equals("vsm")){
                if(boost){
                    Query query = new Query(args[0], index, args[2], metrics_rel_1_2_vsm_boost_filename, metrics_rel_2_vsm_boost_filename);
                    query.readQueryFileVSM(args[1], results_vsm_boost_filename, true);
                    System.out.println("Queries results saved on file \"" + results_vsm_boost_filename + "\"");
                    System.out.println("Metrics (considering relevant documents with relevance 1 and 2) saved on file \"" + metrics_rel_1_2_vsm_boost_filename + "\"");
                    System.out.println("Metrics (considering relevant documents with relevance 2) saved on file \"" + metrics_rel_2_vsm_boost_filename + "\"");
                } else {
                    Query query = new Query(args[0], index, args[2], metrics_rel_1_2_vsm_filename, metrics_rel_2_vsm_filename);
                    query.readQueryFileVSM(args[1], results_vsm_filename, false);
                    System.out.println("Queries results saved on file \"" + results_vsm_filename + "\"");
                    System.out.println("Metrics (considering relevant documents with relevance 1 and 2) saved on file \"" + metrics_rel_1_2_vsm_filename + "\"");
                    System.out.println("Metrics (considering relevant documents with relevance 2) saved on file \"" + metrics_rel_2_vsm_filename + "\"");
                }
            } else {
                if(boost){
                    Query query = new Query(args[0], index, args[2], metrics_rel_1_2_bm25_boost_filename, metrics_rel_2_bm25_boost_filename);
                    query.readQueryFileBM25(args[1], results_bm25_boost_filename, true);
                    System.out.println("Queries results saved on file \"" + results_bm25_boost_filename + "\"");
                    System.out.println("Metrics (considering relevant documents with relevance 1 and 2) saved on file \"" + metrics_rel_1_2_bm25_boost_filename + "\"");
                    System.out.println("Metrics (considering relevant documents with relevance 2) saved on file \"" + metrics_rel_2_bm25_boost_filename + "\"");
                } else {
                    Query query = new Query(args[0], index, args[2], metrics_rel_1_2_bm25_filename, metrics_rel_2_bm25_filename);
                    query.readQueryFileBM25(args[1], results_bm25_filename, false);
                    System.out.println("Queries results saved on file \"" + results_bm25_filename + "\"");
                    System.out.println("Metrics (considering relevant documents with relevance 1 and 2) saved on file \"" + metrics_rel_1_2_bm25_filename + "\"");
                    System.out.println("Metrics (considering relevant documents with relevance 2) saved on file \"" + metrics_rel_2_bm25_filename + "\"");
                }
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
        return indexFile.exists() && indexFile.isFile() && indexDocsFile.exists() && indexDocsFile.isFile();
    }

    public static void createIndex(String stopWordsList, String corpusFile, String rankingMethod) throws IOException {
        index = new Indexer(stopWordsList);
        long startTime = System.nanoTime();
        if(rankingMethod.equals("vsm")) //Vector Space Model
            index.corpusReader(corpusFile, rankingMethod, index_vsm_filename, index_docIDs_vsm_filename);  //Entry point
        else //BM25
            index.corpusReader(corpusFile, rankingMethod, index_bm25_filename, index_docIDs_bm25_filename, bm25_k1, bm25_b);  //Entry point
        long endTime = System.nanoTime();
        if(rankingMethod.equals("vsm")) //Vector Space Model
            System.out.println("Index saved on files \"" + index_vsm_filename + "\" and \"" + index_docIDs_vsm_filename + "\"");
        else //BM25
            System.out.println("Index saved on files \"" + index_bm25_filename + "\" and \"" + index_docIDs_bm25_filename + "\"");
        //Calculate indexing time
        System.out.println("Indexing Time: " + (endTime - startTime) / 1000000000 + " seconds");
    }

    public static void loadIndex(String rankingMethod) throws IOException {
        index = new Indexer();
        long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        if (rankingMethod.equals("vsm")) //Vector Space Model
            index.loadIndexFromFiles(index_vsm_filename, index_docIDs_vsm_filename);  //Entry point
        else //BM25
            index.loadIndexFromFiles(index_bm25_filename, index_docIDs_bm25_filename);  //Entry point
        long endTime = System.nanoTime();
        long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        //Calculate indexing time
        System.out.println("Index loading time: " + (endTime - startTime) / 1000000000 + " seconds");
        //Calculate Memory Usage
        System.out.println("Memory used to load index (roughly): " + (usedMemoryAfter-usedMemoryBefore)/(1024*1024) + " MB");
        //Vocabulary Size
        System.out.println("Vocabulary Size: " + index.getVocabularySize() + " terms");
    }
}
