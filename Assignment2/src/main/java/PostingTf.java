/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

//Posting with term frequency
public class PostingTf {

    private int docID; //Document ID
    private double termFreq; //Term Frequency

    public PostingTf(int docID, double termFreq) {
        this.docID = docID;
        this.termFreq = termFreq;
    }

    public int getDocID() {
        return docID;
    }

    public double getTermFreq() {
        return termFreq;
    }
}