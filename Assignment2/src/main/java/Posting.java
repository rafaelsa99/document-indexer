/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

//Posting with term weight
public class Posting {

    private int docID; //Document ID
    private double termWeight; //Term Weight

    public Posting(int docID, double termFreq) {
        this.docID = docID;
        this.termWeight = termFreq;
    }

    public int getDocID() {
        return docID;
    }

    public double getTermWeight() {
        return termWeight;
    }
}