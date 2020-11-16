import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Index {

    private HashMap<Integer, String> docIDs; // Mapping between the generated ID and the document hash
    private HashMap<String, HashSet<PostingTw>> index; // Inverted Index (weighted)
    private HashMap<String, Double> idf; // Mapping of the idf for each term

    public Index() {
        this.docIDs = new HashMap<>();
        this.index = new HashMap<>();
        this.idf = new HashMap<>();
    }

    public void readIndexFromFile() throws FileNotFoundException {
        final String filePath = "indexFiles/tf_idf_index.txt";
        File file = new File(filePath);
        Scanner sc = new Scanner(file);
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            String[] fields = line.split(";");
            String[] data = fields[0].split(":");
            String term = data[0];
            index.put(term, new HashSet<>());
            idf.put(term, Double.parseDouble(data[1]));
            for (int i = 1; i < fields.length; i++) {
                data = fields[i].split(":");
                index.get(term).add(new PostingTw(Integer.parseInt(data[0]), Double.parseDouble(data[1])));
            }
        }
        sc.close();
    }

    public void readDocIDsFromFile() throws FileNotFoundException {
        final String filePath = "indexFiles/tf_idf_index_doc_ids.txt";
        File file = new File(filePath);
        Scanner sc = new Scanner(file);
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            String[] fields = line.split(";");
            String[] data = fields[0].split(":");
            docIDs.put(Integer.parseInt(data[0]), data[1]);
        }
        sc.close();
    }
}
