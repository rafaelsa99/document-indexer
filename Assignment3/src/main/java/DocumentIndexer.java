import java.io.IOException;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

// This class indexes the documents according to the ranking method and saves the index in a file.
// Even if there is already an index in the file, it is overridden.
public class DocumentIndexer {

    private static final String index_vsm_filename = "indexFiles/index_vsm.txt";
    private static final String index_docIDs_vsm_filename = "indexFiles/index_doc_ids_vsm.txt";
    private static final String index_bm25_filename = "indexFiles/index_bm25.txt";
    private static final String index_docIDs_bm25_filename = "indexFiles/index_doc_ids_bm25.txt";
    private static double bm25_k1 = 1.2;
    private static double bm25_b = 0.75;

    public static void main(String[] args) {
        if (((args.length != 3) && (args.length != 5)) || !(args[2].equalsIgnoreCase("vsm") || args[2].equalsIgnoreCase("bm25"))) {
            System.out.println("Error! Parameters: corpusFilename stopWordsList rankingMethod[\"vsm\" OR \"bm25\"] (Optional: k1 b)");
            return;
        }
        try {
            if(args.length == 5){
                if(Double.parseDouble(args[3]) >= 0)
                    bm25_k1 = Double.parseDouble(args[3]);
                if(Double.parseDouble(args[4]) >= 0 && Double.parseDouble(args[4]) <= 2)
                    bm25_b = Double.parseDouble(args[4]);
            }
            Indexer indexer = new Indexer(args[1]);
            System.out.println("Indexing corpus...");
            long startTime = System.nanoTime();
            if(args[2].equalsIgnoreCase("vsm")) //Vector Space Model
                indexer.corpusReader(args[0], args[2].toLowerCase(), index_vsm_filename, index_docIDs_vsm_filename);  //Entry point
            else //BM25
                indexer.corpusReader(args[0], args[2].toLowerCase(), index_bm25_filename, index_docIDs_bm25_filename, bm25_k1, bm25_b);  //Entry point
            long endTime = System.nanoTime();
            if(args[2].equalsIgnoreCase("vsm")) //Vector Space Model
                System.out.println("Index saved on files \"" + index_vsm_filename + "\" and \"" + index_docIDs_vsm_filename + "\"");
            else //BM25
                System.out.println("Index saved on files \"" + index_bm25_filename + "\" and \"" + index_docIDs_bm25_filename + "\"");
            //Calculate indexing time
            System.out.println("Indexing Time: " + (endTime - startTime) / 1000000000 + " seconds");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
