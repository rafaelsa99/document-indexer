import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import java.io.*;
import java.util.*;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Tokenizer {

    SnowballStemmer stemmer;    //Snowball Stemmer
    HashSet<String> stopWords;  //Stop Words

    public Tokenizer(String stopWordsFilename) throws IOException {
        stemmer = new englishStemmer();
        stopWords = new HashSet<>();
        loadStopWords(stopWordsFilename);
    }

    //Load stop words from file to data structure
    public void loadStopWords(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
        String line;
        while((line = reader.readLine()) != null)
            stopWords.add(line);
        reader.close();
    }

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

    public HashSet<String> improvedTokenizer(Document doc){
        String title = doc.getTitle();
        String abs = doc.getAbstrct();
        //Set title and abstract with lowercase
        title = title.toLowerCase();
        abs = abs.toLowerCase();
        //Tokenizer Decisions
        title = replaceNonAlphaBySpace(title);
        abs = replaceNonAlphaBySpace(abs);
        //Split title and abstract on whitespace
        List<String> titleTokens = splitOnWhitespace(title);
        List<String> abstractTokens = splitOnWhitespace(abs);
        //Stemming
        titleTokens = applyStemming(titleTokens);
        abstractTokens = applyStemming(abstractTokens);
        //Add all tokens to Set
        HashSet<String> tokens = new HashSet<>(titleTokens);
        tokens.addAll(abstractTokens);
        //Remove the stop words from the tokens set
        tokens.removeIf(token -> stopWords.contains(token));
        return tokens;
    }

    //Apply Stemming to the list of tokens
    public List<String> applyStemming(List<String> tokens){
        List<String> stemmedTokens = new ArrayList<>();
        for (String token:tokens) {
            stemmer.setCurrent(token);
            stemmer.stem();
            stemmedTokens.add(stemmer.getCurrent());
        }
        return stemmedTokens;
    }
}
