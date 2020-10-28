/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Posting {
    private final int docID;  // Generated ID of the document
    private final int nOccur; // Number of occurrences of the token in the document

    public Posting(int docID, int nOccur) {
        this.docID = docID;
        this.nOccur = nOccur;
    }

    public int getDocID() { return docID; }

    public int getnOccur() { return nOccur; }
}
