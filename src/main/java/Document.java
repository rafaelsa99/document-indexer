/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Document {
    private String id;      //DOI of the document
    private String title;   //Title of the document
    private String abstrct; //Abstract of the document

    public Document(String id, String title, String abstrct) {
        this.id = id;
        this.title = title;
        this.abstrct = abstrct;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstrct() {
        return abstrct;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAbstrct(String abstrct) {
        this.abstrct = abstrct;
    }
}
