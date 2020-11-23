import java.io.IOException;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class DocumentIndexer {

    private static final String index_tf_idf_filename = "indexFiles/tf_idf_index.txt";
    private static final String index_docIDs_tf_idf_filename = "indexFiles/tf_idf_index_doc_ids.txt";
    private static final String index_bm25_filename = "indexFiles/bm25_index.txt";
    private static final String index_docIDs_bm25_filename = "indexFiles/bm25_index_doc_ids.txt";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Error! Parameters: corpusFile stopWordsList");
            return;
        }
        try {
            Indexer indexer = new Indexer(args[1]);
            long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long startTime = System.nanoTime();
            indexer.corpusReader(args[0], index_tf_idf_filename, index_docIDs_tf_idf_filename);  //Entry point
            long endTime = System.nanoTime();
            long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            //Calculate indexing time
            System.out.println("Indexing Time: " + (endTime - startTime) / 1000000000 + " seconds");
            //Calculate Memory Usage
            System.out.println("Memory used: " + (usedMemoryAfter-usedMemoryBefore)/(1024*1024) + " MB");
            //Vocabulary Size
            System.out.println("Vocabulary Size: " + indexer.getVocabularySize() + " terms");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
