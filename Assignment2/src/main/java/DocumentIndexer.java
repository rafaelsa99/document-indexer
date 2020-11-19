import java.io.IOException;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class DocumentIndexer {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Error! Parameters: corpusFile stopWordsList queries.txt index Method(tf-idf or BM25)");
            return;
        }
        try {
            Query query = new Query(args[1]);
            query.readQueryFile(args[2]);
            Indexer indexer = new Indexer(args[1],query);

            long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long startTime = System.nanoTime();
            indexer.corpusReader(args[0]);  //Entry point

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
