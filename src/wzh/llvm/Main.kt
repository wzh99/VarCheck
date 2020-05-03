package wzh.llvm

import wzh.llvm.parse.Builder
import wzh.llvm.parse.Lexer
import wzh.llvm.parse.Parser
import wzh.llvm.plot.Plotter

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val lexer = Lexer(args[0])
            val parser = Parser(lexer)
            val ast = parser.parseModule()
            val builder = Builder(ast)
            val module = builder.build()
            val plotter = Plotter(module)
            plotter.plot("out")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
