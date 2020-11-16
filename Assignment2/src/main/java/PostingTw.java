/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

//Posting with term weights
public class PostingTw {

    private int docID; //Document ID
    private double termWeight; //Term Weight

    public PostingTw(int docID, double termWeight) {
        this.docID = docID;
        this.termWeight = termWeight;
    }

    public int getDocID() {
        return docID;
    }

    public double getTermWeight() {
        return termWeight;
    }
}
