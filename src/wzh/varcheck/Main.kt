package wzh.varcheck

import wzh.varcheck.parse.Lexer
import wzh.varcheck.parse.Parser

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val lexer = Lexer("test/uninit.ll")
            val parser = Parser(lexer)
            parser.parseModule()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
