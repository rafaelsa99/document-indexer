import java.io.IOException;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class DocumentIndexer {
    public static void main(String[] args) throws IOException {
        /*if (args.length != 1) {
            System.out.println("Error! Parameters: corpus");
            return;
        }*/
        CorpusReader corpusReader = new CorpusReader();
        corpusReader.loadFile("all_sources_metadata_2020-03-13.xlsx");

        Indexer indexer = new Indexer();
        long freeMemoryBefore = Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        //indexer.makeIndex(args[0]);
        indexer.makeIndex(corpusReader);
        long endTime = System.nanoTime();
        long freeMemoryAfter = Runtime.getRuntime().freeMemory();
        //Calculate indexing time
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("Indexing Time: " + duration + " milliseconds");
        //Calculate Memory Usage
        System.out.println("Memory used: " + (freeMemoryBefore-freeMemoryAfter));

    }
}
