import java.io.IOException;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class DocumentIndexer {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Error! Parameters: corpusFile stopWordsList");
            return;
        }
        try {
            Indexer indexer = new Indexer(args[1]);
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
            //List the ten first terms (in alphabetic order) that appear in only one document
            System.out.println("Ten first terms (in alphabetic order) that appear in only one document:");
            int num = 1;
            for (String t:indexer.getTop10TermsDocFreqOne()) {
                System.out.println("\t" + num++ + ": " + t + " (Doc. Frequency = " + indexer.getDocFreq(t) + ")");
            }
            //List the ten terms with highest document frequency
            System.out.println("Ten terms with highest document frequency:");
            num = 1;
            for (String t:indexer.getTop10Terms()) {
                System.out.println("\t" + num++ + ": " + t + " (Doc. Frequency = " + indexer.getDocFreq(t) + ")");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
