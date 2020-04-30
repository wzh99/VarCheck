package wzh.varcheck.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Lexer {

    private String src;
    private int ptr = 0;
    private Location loc;
    private StringBuffer buf;
    private TokenTag tag;

    public Lexer(String path) throws IOException {
        // Read all characters from file
        var file = new File(path);
        var ifs = new FileInputStream(file);
        var len = ifs.available();
        var bytes = new byte[len];
        var read = ifs.read(bytes);
        if (read == -1)
            throw new IOException("No characters can be read from file");
        ifs.close();
        src = new String(bytes, StandardCharsets.UTF_8);
    }

     Token next() {
        // Initialize fields for lexing
        buf = new StringBuffer();
        tag = null;

        // Iterate until all characters are consumed
        while (ptr < src.length()) {
            var c = peek();
            if (tag == null) { // beginning of new token
                switch (c) {
                    case '@':
                        read();
                        if (!isAlphaDigitMark(peek())) error(ALPHA_DIGIT_MARK);
                        read();
                        tag = TokenTag.GLOBAL_ID;
                        continue;

                    case '%':
                        read();
                        if (!isAlphaDigitMark(peek())) error(ALPHA_DIGIT_MARK);
                        read();
                        tag = TokenTag.LOCAL_ID;
                        continue;

                    case '#':
                        read();
                        if (!Character.isDigit(peek())) error(DIGIT);
                        read();
                        tag = TokenTag.ATTRIBUTE_GROUP;
                        continue;
                }
            }
        }
        return null;
    }

    // Create a token from buffer content
    private Token createFromBuffer() {
        return new Token(getLoc(), tag, buf.toString());
    }

    // Read one character to buffer
    private void read() {
        buf.append(shift());
    }

    // Skip one character
    private void skip() {
        shift();
    }

    // Read the character currently pointed to. Move the pointer one character forward.
    private char shift() {
        var c = src.charAt(ptr++);
        if (c == '\n') loc.newLine(); else loc.shift();
        return c;
    }

    // Look ahead one character in the source. If EOF is reached, return 0
    private char peek() {
        return (ptr < src.length()) ? src.charAt(ptr) : '\0';
    }

    // Report lexing error by raising an exception
    private void error(String expect) throws ParseError {
        throw new ParseError(getLoc(), String.format("Expect %s, got %s.", expect, peek()));
    }

    private static final String DIGIT = "[0-9]";
    private static final String ALPHA_MARK = "[A-Za-z._]";
    private static final String ALPHA_DIGIT_MARK = "[A-Za-z0-9._]";

    private static boolean isMark(char c) {
        return c == '.' || c == '_';
    }

    private static boolean isAlphaMark(char c) {
        return isMark(c) || Character.isUpperCase(c) || Character.isLowerCase(c);
    }

    private static boolean isAlphaDigitMark(char c) {
        return isAlphaMark(c) || Character.isDigit(c);
    }

    private Location getLoc() {
        try {
            return (Location) loc.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class Token {
    Location loc;
    TokenTag tag;
    String str;

    Token(Location loc, TokenTag tag, String str) {
        this.loc = loc;
        this.tag = tag;
        this.str = str;
    }

    @Override
    public String toString() {
        return "Token{" +
                "loc=" + loc +
                ", tag=" + tag +
                ", str='" + str + '\'' +
                '}';
    }
}

enum TokenTag {
    RESERVED,  // [A-Za-z._][A-Za-z0-9._]*
    GLOBAL_ID,  // @[A-Za-z0-9._]+
    LOCAL_ID,  // %[A-Za-z0-9._]+
    DIGITS,  // [0-9]+
    ATTRIBUTE_GROUP,  // #[0-9]+
    EQUAL,  // =
    COMMA,  // ,
    COLON,  // :
    SEMICOLON,  // ;
    ASTERISK,  // *
    LEFT_ROUND,  // (
    RIGHT_ROUND,  // )
    LEFT_SQUARE,  // [
    RIGHT_SQUARE,  // ]
    LEFT_CURLY,  // {
    RIGHT_CURLY,  // }
    NEW_LINE // \n
}
