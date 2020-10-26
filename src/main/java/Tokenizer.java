import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Tokenizer {

    public Tokenizer() { }

    public HashSet<String> simpleTokenizer(Document doc){
        String title = doc.getTitle();
        String abs = doc.getAbstrct();
        //Set title and abstract with lowercase
        title = title.toLowerCase();
        abs = abs.toLowerCase();
        //Replace all non-alphanumeric characters by a space
        title = title.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", " ");
        abs = abs.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", " ");
        //Split title and abstract on whitespace
        String[] titleWords = title.trim().split("\\s+");
        List<String> abstractWords = Arrays.asList(abs.trim().split("\\s+"));
        //Add all tokens to Set
        HashSet<String> tokens = new HashSet<>(Arrays.asList(titleWords));
        tokens.addAll(abstractWords);
        //Remove tokens with less than 3 characters
        Iterator<String> iter = tokens.iterator();
        while (iter.hasNext()){
            String str = iter.next();
            if(str.length() < 3)
                iter.remove();
        }
        return tokens;
    }
}
