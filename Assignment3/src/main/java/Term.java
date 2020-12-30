import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

//Term with frequency/weight and positions
public class Term {

    private String term;
    private double termValue; //Term Weight OR Term Frequency
    private List<Integer> positions;

    public Term(String term, int position) {
        this.term = term;
        this.termValue = 1;
        this.positions = new ArrayList<>();
        this.positions.add(position);
    }

    public String getTerm() {
        return term;
    }

    public double getTermValue() {
        return termValue;
    }

    public void setTermValue(double termValue) {
        this.termValue = termValue;
    }

    public void addPosition(int position) {
        this.positions.add(position);
    }

    public void incrementFrequency(){
        this.termValue += 1;
    }

    public List<Integer> getPositions() {
        return positions;
    }
}
