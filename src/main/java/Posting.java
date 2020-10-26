/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Posting {
    private int docID;  // Generated ID of the document
    private int nOccur; // Number of occurrences of the token in the document

    public Posting(int docID, int nOccur) {
        this.docID = docID;
        this.nOccur = nOccur;
    }
}
