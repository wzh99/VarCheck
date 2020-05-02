package wzh.varcheck

import wzh.varcheck.parse.Builder
import wzh.varcheck.parse.Lexer
import wzh.varcheck.parse.Parser

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val lexer = Lexer("test/fib.ll")
            val parser = Parser(lexer)
            val ast = parser.parseModule()
            val builder = Builder(ast)
            val module = builder.build()
            println()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
