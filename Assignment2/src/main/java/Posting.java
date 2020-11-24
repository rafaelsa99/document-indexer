/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

//Posting with term weight/term frequency
public class Posting {

    private int docID; //Document ID
    private double termValue; //Term Weight OR Term Frequency

    public Posting(int docID, double termValue) {
        this.docID = docID;
        this.termValue = termValue;
    }

    public int getDocID() {
        return docID;
    }

    public double getTermValue() {
        return termValue;
    }
}