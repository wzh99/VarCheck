package wzh.varcheck.parse;

public class Location implements Cloneable {
    int line;
    int column;

    Location(int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    void newLine() {
        column = 0;
        line++;
    }

    void shift() { column++; }
}
