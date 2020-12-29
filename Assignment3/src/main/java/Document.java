/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

//Represents a line of the corpus: a document
public class Document {
    private final String id;      //DOI of the document
    private final String title;   //Title of the document
    private final String abstrct; //Abstract of the document

    public Document(String id, String title, String abstrct) {
        this.id = id;
        this.title = title;
        this.abstrct = abstrct;
    }

    public String getId() { return id; }

    public String getText(){
        return title + " " + abstrct;
    }
}