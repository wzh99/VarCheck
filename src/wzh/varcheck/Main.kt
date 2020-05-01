package wzh.varcheck

import wzh.varcheck.parse.Lexer
import wzh.varcheck.parse.Parser
import java.io.IOException
import java.lang.Exception

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val lexer = Lexer("test/fib.ll")
            val parser = Parser(lexer)
            parser.parseModule()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
