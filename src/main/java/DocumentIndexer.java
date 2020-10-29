import java.io.IOException;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class DocumentIndexer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Error! Parameters: corpus");
            return;
        }
        Indexer indexer = new Indexer();
        long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        try {
            indexer.corpusReader(args[0]);
            long endTime = System.nanoTime();
            long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            //Calculate indexing time
            long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
            System.out.println("Indexing Time: " + duration/1000 + " seconds");
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
