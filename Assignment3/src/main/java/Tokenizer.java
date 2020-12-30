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
    public ArrayList<String> splitOnWhitespace(String str) {
        return new ArrayList<>(Arrays.asList(str.trim().split("\\s+")));
    }

    public List<Term> simpleTokenizer(String text){
        //Set text with lowercase
        text = text.toLowerCase();
        //Replace all non-alphabetic characters by a space
        text = replaceNonAlphaBySpace(text);
        //Split text on whitespace
        ArrayList<String> textTokens = splitOnWhitespace(text);
        //Remove tokens with less than 3 characters
        textTokens.removeIf(entry -> entry.length() < 3);
        //Add all tokens to list with frequencies and positions
        List<Term> tokens = getFrequenciesAndPositions(textTokens);
        return tokens;
    }

    public List<Term> improvedTokenizer(String text){
        //Set text with lowercase
        text = text.toLowerCase();
        //Tokenizer Decisions
        text = replaceNonAlphaBySpace(text);
        //Split text on whitespace
        ArrayList<String> textTokens = splitOnWhitespace(text);
        //Remove the stop words from the tokens set
        textTokens.removeIf(entry -> stopWords.contains(entry));
        //Stemming
        textTokens = applyStemming(textTokens);
        //Add all tokens to list with frequencies and positions
        List<Term> tokens = getFrequenciesAndPositions(textTokens);
        return tokens;
    }

    public HashMap<String, Integer> improvedTokenizerforQuery(String query){
        //Set queryterm with lowercase
        String termQuery = query.toLowerCase();
        //Tokenizer Decisions
        termQuery = replaceNonAlphaBySpace(termQuery);
        //Split title and abstract on whitespace
        ArrayList<String> queryTokens = splitOnWhitespace(termQuery);
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
        ArrayList<String> queryTokens = splitOnWhitespace(termQuery);
        //Stemming
        queryTokens = applyStemming(queryTokens);
        //Remove the stop words from the tokens list
        queryTokens.removeIf(entry -> stopWords.contains(entry));
        return queryTokens;
    }

    //Apply Stemming to the list of tokens
    public ArrayList<String> applyStemming(ArrayList<String> tokens){
        ArrayList<String> stemmedTokens = new ArrayList<>();
        for (String token:tokens) {
            stemmer.setCurrent(token);
            stemmer.stem();
            stemmedTokens.add(stemmer.getCurrent());
        }
        return stemmedTokens;
    }

    //Add tokens to List with frequencies and positions
    public List<Term> getFrequenciesAndPositions(List<String> listTokens){
        HashMap<String, Term> tokens = new HashMap<>();
        for(int i = 0; i < listTokens.size(); i++) {
            String token = listTokens.get(i);
            if(tokens.containsKey(token)) {
                tokens.get(token).incrementFrequency();
                tokens.get(token).addPosition(i);
            } else
                tokens.put(token, new Term(token, i));
        }

        return new ArrayList<>(tokens.values());
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