import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Tokenizer {

    public Tokenizer() { }

    //Replace all non-alphabetic characters by a space
    public String replaceNonAlphaBySpace(String str){
        return str.replaceAll("[^\\p{IsAlphabetic}]", " ");
    }

    //Split string on whitespace
    public List<String> splitOnWhitespace(String str) {
        return Arrays.asList(str.trim().split("\\s+"));
    }

    public HashSet<String> simpleTokenizer(Document doc){
        String title = doc.getTitle();
        String abs = doc.getAbstrct();
        //Set title and abstract with lowercase
        title = title.toLowerCase();
        abs = abs.toLowerCase();
        //Replace all non-alphabetic characters by a space
        title = replaceNonAlphaBySpace(title);
        abs = replaceNonAlphaBySpace(abs);
        //Split title and abstract on whitespace
        List<String> titleTokens = splitOnWhitespace(title);
        List<String> abstractTokens = splitOnWhitespace(abs);
        //Add all tokens to Set
        HashSet<String> tokens = new HashSet<>(titleTokens);
        tokens.addAll(abstractTokens);
        //Remove tokens with less than 3 characters
        tokens.removeIf(str -> str.length() < 3);
        return tokens;
    }
}
