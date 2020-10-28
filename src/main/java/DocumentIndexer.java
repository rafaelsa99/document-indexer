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
        //indexer.makeIndex(corpusReader);
        indexer.makeIndex("ola");
        long endTime = System.nanoTime();
        long freeMemoryAfter = Runtime.getRuntime().freeMemory();
        //Calculate indexing time
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("Indexing Time: " + duration + " milliseconds");
        //Calculate Memory Usage
        System.out.println("Memory used: " + (freeMemoryBefore-freeMemoryAfter));
        //Vocabulary Size
        System.out.println("Vocabulary Size: " + indexer.getVocabularySize() + " terms");
        //List the ten first terms (in alphabetic order) that appear in only one document
        System.out.println("Ten first terms (in alphabetic order) that appear in only one document:");
        int num = 1;
        for (Term t:indexer.getTop10TermsDocFreqOne()) {
            System.out.println("\t" + num++ + ": " + t.getTerm() + " (Doc. Frequency = " + t.getFrequency() + ")");
        }
        //List the ten terms with highest document frequency
        System.out.println("Ten terms with highest document frequency:");
        num = 1;
        for (Term t:indexer.getTop10Terms()) {
            System.out.println("\t" + num++ + ": " + t.getTerm() + " (Doc. Frequency = " + t.getFrequency() + ")");
        }

    }
}
