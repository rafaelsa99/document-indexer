/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Subindex {

    private String startingLetter;
    private String endingLetter;
    private String filename;

    public Subindex(String startingLetter, String endingLetter, String filename) {
        this.startingLetter = startingLetter;
        this.endingLetter = endingLetter;
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public String getStartingLetter() {
        return startingLetter;
    }

    public String getEndingLetter() {
        return endingLetter;
    }
}
