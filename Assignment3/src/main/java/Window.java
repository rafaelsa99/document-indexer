import java.util.List;

/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */


public class Window {
    private String term;
    private List<Integer> positions;

    public Window(String term, List<Integer> positions) {
        this.term = term;
        this.positions = positions;
    }

    public String getTerm() {
        return term;
    }

    public int getPositionsSize(){
        return positions.size();
    }

    public int getPosition(int pos) {
        return positions.get(pos);
    }

    public List<Integer> getPositions() {
        return positions;
    }
}
