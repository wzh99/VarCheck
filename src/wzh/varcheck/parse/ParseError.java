package wzh.varcheck.parse;

public class ParseError extends RuntimeException {

    public final Location loc;
    public final String msg;

    ParseError(Location loc, String msg) {
        super();
        this.loc = loc;
        this.msg = msg;
    }
}
