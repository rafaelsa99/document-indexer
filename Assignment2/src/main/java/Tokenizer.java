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

    public HashMap<String, Integer> simpleTokenizer(Document doc){
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
        //Add all tokens to Map and counts the frequency
        HashMap<String, Integer> tokens = countTokensFrequencies(titleTokens, abstractTokens);
        //Remove tokens with less than 3 characters
        tokens.entrySet().removeIf(entry -> entry.getKey().length() < 3);
        return tokens;
    }

    public HashMap<String, Integer> improvedTokenizer(Document doc){
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
        //Add all tokens to Map and count Frequency
        HashMap<String, Integer> tokens = countTokensFrequencies(titleTokens, abstractTokens);
        //Remove the stop words from the tokens set
        tokens.entrySet().removeIf(entry -> stopWords.contains(entry.getKey()));
        return tokens;
    }

    public List<String> improvedTokenizerforQuery(String query){
        //Set queryterm with lowercase
        String termQuery = query.toLowerCase();
        //Tokenizer Decisions
        termQuery = replaceNonAlphaBySpace(termQuery);
        //Split title and abstract on whitespace
        List<String> queryTokens = splitOnWhitespace(termQuery);
        //Stemming
        queryTokens = applyStemming(queryTokens);
        //Add all tokens to Map
        queryTokens.removeIf(entry -> stopWords.contains(entry));
        //Remove the stop words from the tokens set
        return queryTokens;
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

    //Add tokens to HashMap while counting frequency
    public HashMap<String, Integer> countTokensFrequencies(List<String> titleTokens, List<String> abstractTokens){
        HashMap<String, Integer> tokens = new HashMap<>();
        for (String titleToken:titleTokens) {
            if(tokens.containsKey(titleToken))
                tokens.replace(titleToken, (tokens.get(titleToken) + 1));
            else
                tokens.put(titleToken, 1);
        }
        for (String abstractToken:abstractTokens) {
            if(tokens.containsKey(abstractToken))
                tokens.replace(abstractToken, (tokens.get(abstractToken) + 1));
            else
                tokens.put(abstractToken, 1);
        }
        return tokens;
    }
}