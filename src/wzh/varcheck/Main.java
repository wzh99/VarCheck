package wzh.varcheck;

import wzh.varcheck.parse.Lexer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            var lexer = new Lexer("test/uninit.ll");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
