import java.io.File;
import java.io.IOException;
import java.util.*;

public class Query {
    private List<String> terms;
    private final String stopWordsList;
    public Query(String stopWords)
    {
        this.stopWordsList = stopWords;
    }

    public void useTokenizer(String queryterm) throws IOException {
        Tokenizer tokenizer = new Tokenizer(stopWordsList);
        terms = tokenizer.improvedTokenizerforQuery(queryterm); //obtain tokens
    }

    public void readQueryFile(String file) throws IOException
    {
        File myObj = new File(file);
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            useTokenizer(data);
        }
        myReader.close();
    }

    public List<String> getTerms() { return terms; }

    public void setTerms(List<String> terms) { this.terms = terms; }
}
