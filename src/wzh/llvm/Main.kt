package wzh.llvm

import wzh.llvm.analysis.VarChecker
import wzh.llvm.parse.Builder
import wzh.llvm.parse.Lexer
import wzh.llvm.parse.Parser
import wzh.llvm.plot.Plotter
import kotlin.system.exitProcess


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val opt = parseArgs(args)
        try {
            val lexer = Lexer(opt.input)
            val parser = Parser(lexer)
            val ast = parser.parseModule()
            val builder = Builder(ast)
            val module = builder.build()
            if (!opt.plotDir.isNullOrEmpty()) {
                val plotter = Plotter(module)
                plotter.plot(opt.plotDir)
            }
            val checker = VarChecker()
            checker.check(module)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseArgs(args: Array<String>): Option {
        if (args.isEmpty()) printHelp()
        var plotDir: String? = null
        if (args[1] == "-p") plotDir = args[2]
        return Option(args[0], plotDir)
    }

    private val helpMsg = arrayOf(
            "usage: java -jar VarCheck.jar input_file [options]",
            "-p dir  output LaTeX plotting source in given directory"
    )

    private fun printHelp() {
        println(helpMsg.joinToString(separator = "\n"))
        exitProcess(0)
    }
}

private class Option(val input: String, val plotDir: String?)
