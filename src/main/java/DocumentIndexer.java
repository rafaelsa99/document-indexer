import java.util.HashSet;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class DocumentIndexer {
    public static void main(String[] args) {
        /*if (args.length != 1) {
            System.out.println("Error! Parameters: corpus");
            return;
        }
        Indexer indexer = new Indexer();
        indexer.makeIndex(args[0]);*/
        Tokenizer t = new Tokenizer();
        Document d = new Document("12345", "    Ola ola ola meu mundo   lindo! Como está o meu mundo?  ", "Isto e um abs");
        HashSet<String> s = t.simpleTokenizer(d);
        System.out.println(s);

    }
}
