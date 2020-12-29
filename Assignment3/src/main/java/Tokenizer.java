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

    public HashMap<String, Integer> simpleTokenizer(String text){
        //Set text with lowercase
        text = text.toLowerCase();
        //Replace all non-alphabetic characters by a space
        text = replaceNonAlphaBySpace(text);
        //Split text on whitespace
        List<String> textTokens = splitOnWhitespace(text);
        //Add all tokens to Map and counts the frequency
        HashMap<String, Integer> tokens = countTokensFrequencies(textTokens);
        //Remove tokens with less than 3 characters
        tokens.entrySet().removeIf(entry -> entry.getKey().length() < 3);
        return tokens;
    }

    public HashMap<String, Integer> improvedTokenizer(String text){
        //Set text with lowercase
        text = text.toLowerCase();
        //Tokenizer Decisions
        text = replaceNonAlphaBySpace(text);
        //Split text on whitespace
        List<String> textTokens = splitOnWhitespace(text);
        //Stemming
        textTokens = applyStemming(textTokens);
        //Add all tokens to Map and count Frequency
        HashMap<String, Integer> tokens = countTokensFrequencies(textTokens);
        //Remove the stop words from the tokens set
        tokens.entrySet().removeIf(entry -> stopWords.contains(entry.getKey()));
        return tokens;
    }

    public HashMap<String, Integer> improvedTokenizerforQuery(String query){
        //Set queryterm with lowercase
        String termQuery = query.toLowerCase();
        //Tokenizer Decisions
        termQuery = replaceNonAlphaBySpace(termQuery);
        //Split title and abstract on whitespace
        List<String> queryTokens = splitOnWhitespace(termQuery);
        //Stemming
        queryTokens = applyStemming(queryTokens);
        //Add all tokens to Map
        HashMap<String, Integer> tokens = countTokensFrequenciesDoc(queryTokens);
        //Remove the stop words from the tokens set
        tokens.entrySet().removeIf(entry -> stopWords.contains(entry.getKey()));
        return tokens;
    }

    public List<String> improvedTokenizerforQueryBM25(String query){
        //Set queryterm with lowercase
        String termQuery = query.toLowerCase();
        //Tokenizer Decisions
        termQuery = replaceNonAlphaBySpace(termQuery);
        //Split title and abstract on whitespace
        List<String> queryTokens = splitOnWhitespace(termQuery);
        //Stemming
        queryTokens = applyStemming(queryTokens);
        //Remove the stop words from the tokens list
        queryTokens.removeIf(entry -> stopWords.contains(entry));
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
    public HashMap<String, Integer> countTokensFrequencies(List<String> listTokens){
        HashMap<String, Integer> tokens = new HashMap<>();
        for (String titleToken:listTokens) {
            if(tokens.containsKey(titleToken))
                tokens.replace(titleToken, (tokens.get(titleToken) + 1));
            else
                tokens.put(titleToken, 1);
        }
        return tokens;
    }

    //Add tokens to HashMap while counting frequency
    public HashMap<String, Integer> countTokensFrequenciesDoc(List<String> stringTerms){
        HashMap<String, Integer> tokens = new HashMap<>();
        for (String termToken:stringTerms) {
            if(tokens.containsKey(termToken))
                tokens.replace(termToken, (tokens.get(termToken) + 1));
            else
                tokens.put(termToken, 1);
        }
        return tokens;
    }
}