/**
 *
 * @author Rafael Sá 104552 and António Ramos 101193
 */

public class Term implements Comparable{
    private String term;
    private int frequency;   // Number of occurrences of the term

    public Term(String term, int frequency) {
        this.term = term;
        this.frequency = frequency;
    }

    public Term(String term) {
        this.term = term;
        this.frequency = 0;
    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Term other = (Term) obj;
        if(!term.equals(other.term))
            return false;
        return true;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getFrequency() {
        return frequency;
    }

    public void incrementFrequency() {
        this.frequency += 1;
    }

    @Override
    public int compareTo(Object t) {
        return term.compareTo(((Term)t).term);
    }
}
