import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

//Posting with term weight/term frequency
public class Posting {

    private int docID; //Document ID
    private double termValue; //Term Weight OR Term Frequency
    private List<Integer> positions;

    public Posting(int docID, double termValue, List<Integer> positions) {
        this.docID = docID;
        this.termValue = termValue;
        this.positions = new ArrayList<>(positions);
    }

    public int getDocID() {
        return docID;
    }

    public double getTermValue() {
        return termValue;
    }

    public void setTermValue(double termValue) {
        this.termValue = termValue;
    }

    public List<Integer> getPositions() {
        return positions;
    }
}