
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
        indexer.makeIndex(args[0]);
    }
}
