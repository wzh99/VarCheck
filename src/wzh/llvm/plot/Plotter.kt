package wzh.llvm.plot

import wzh.llvm.lang.BasicBlock
import wzh.llvm.lang.Func
import wzh.llvm.lang.Instruction
import wzh.llvm.lang.Module
import java.io.FileWriter
import java.nio.file.Path

class Plotter(private val module: Module) {

    private val fileName = "${module.name}.tex"

    fun plot(dir: String) {
        // Compile to LaTeX code
        val moduleStr = module.func.map(this::plotFunc).fold("", String::plus)
        val fileStr = MODULE_TEMPLATE.replace(MODULE_INSERT, moduleStr)

        // Create file in given path
        val path = Path.of(dir, fileName)
        val file = path.toFile()
        val writer = FileWriter(file)
        writer.write(fileStr)
        writer.close()
    }

    // Each function is plotted in a separate section of LaTeX document.
    private fun plotFunc(func: Func): String {
        // Perform BFS on the graph
        val levels = ArrayList<ArrayList<BasicBlock>>()
        for (elem in BfsIterator(func)) {
            if (elem.level >= levels.size)
                levels.add(ArrayList())
            levels[elem.level].add(elem.block)
        }

        // Create node for each block
        val nodes = HashMap<BasicBlock, FigureNode>()
        for (l in levels.indices) {
            for (i in levels[l].indices) {
                val bb = levels[l][i]
                val text = bb.inst.map(Instruction::toString) as ArrayList<String>
                text.add(0, "${bb.name}:")
                nodes[bb] = FigureNode(l, i, text)
            }
        }

        // Add node to graph
        val graphDef = StringBuilder()
        for (l in levels.indices) {
            val maxHeightBb = levels[l].maxBy { b -> nodes[b]!!.height }!!
            val maxHeight = nodes[maxHeightBb]!!.height
            for (i in levels[l].indices) {
                val bb = levels[l][i]
                val node = nodes[bb]!!
                val text = node.text.joinToString("\\\\", transform = Companion::toLaTeX)
                val str = if (i == 0) { // first node of this level
                    if (l == 0) { // entry node
                        "\\node[block](${nodeName(bb)}){$text};"
                    } else {
                        val aboveMaxHeightBb = levels[l - 1].maxBy { b -> nodes[b]!!.height }!!
                        val aboveMaxHeight = nodes[aboveMaxHeightBb]!!.height
                        val aboveBb = levels[l - 1][i]
                        val dist = 6 * (maxHeight + aboveMaxHeight) + 10
                        "\\node[block, below of=${nodeName(aboveBb)}, " +
                                "node distance=${dist}pt](${nodeName(bb)}){$text};"
                    }
                } else { // other nodes of this level
                    val leftBb = levels[l][i - 1]
                    val leftNode = nodes[leftBb]!!
                    val dist = (3 * (node.width + leftNode.width) + 30)
                    "\\node[block, right of=${nodeName(leftBb)}, node distance=${dist}pt]" +
                            "(${nodeName(bb)}){$text};"
                }
                graphDef.append(str + '\n')
            }
        }

        // Connect blocks with arrows
        nodes.forEach { (block, node) ->
            block.succ.forEach { succ ->
                val succNode = nodes[succ]!!
                val str = if (succNode.level > node.level)  // flow to level below
                    "\\draw[->]({${nodeName(block)}}.south)--({${nodeName(succ)}}.north);"
                else if (succNode.level == node.level)  // the same level
                    if (succNode.index > node.index) // on right side
                        "\\draw[->]({${nodeName(block)}}.east)--({${nodeName(succ)}}.west);"
                    else // left side
                        "\\draw[->]({${nodeName(block)}}.west)--({${nodeName(succ)}}.east);"
                else// to level above, usually a back edge
                    if (succNode.index > node.index) // flow to top-right corner
                        "\\draw[->]({${nodeName(block)}}.east)--({${nodeName(succ)}}.west);"
                    else if (succNode.index < node.index) // to top-left corner
                        "\\draw[->]({${nodeName(block)}}.west)--({${nodeName(succ)}}.east);"
                    else {
                        val ratio = node.index.toFloat() / levels[node.level].size
                        if (ratio < 0.5)
                            "\\draw[->]({${nodeName(block)}}.west)to[out=180, in=180]" +
                                    "({${nodeName(succ)}}.west);"
                        else
                            "\\draw[->]({${nodeName(block)}}.east)to[out=0, in=0]" +
                                    "({${nodeName(succ)}}.east);"
                    }
                graphDef.append(str + '\n')
            }
        }

        return FUNC_TEMPLATE
                .replace(FUNC_SIG_INSERT, toLaTeX(func.toString()))
                .replace(FUNC_BODY_INSERT, graphDef.toString())
    }

    companion object {
        private val ESCAPE_REGEX = Regex("[\\s%]")

        private fun toLaTeX(s: String): String {
            return s.replace(ESCAPE_REGEX) { m -> "\\" + m.value }
        }

        private fun nodeName(block: BasicBlock): String {
            return block.name.replace('.', '_')
        }

        private const val MODULE_INSERT = "@MODULE"
        private const val MODULE_TEMPLATE =
                "\\documentclass[a4paper]{article}\n" +
                        "\\usepackage{geometry}\n" +
                        "\\usepackage{tikz}\n" +
                        "\\usetikzlibrary{arrows, shapes}\n" +
                        "\\tikzstyle{block}=[rectangle, align=left, draw]\n" +
                        "\\geometry{left=2.5cm, right=2.5cm, top=2.5cm, bottom=2.5cm}\n" +
                        "\\begin{document}\n" +
                        "\\tt\n" +
                        MODULE_INSERT +
                        "\\end{document}"
        private const val FUNC_SIG_INSERT = "@FUNC_SIG"
        private const val FUNC_BODY_INSERT = "@FUNC_BODY"
        private const val FUNC_TEMPLATE =
                "\\section{\\texttt{" + FUNC_SIG_INSERT + "}}\n" +
                        "\\begin{tikzpicture}\n" +
                        FUNC_BODY_INSERT +
                        "\\end{tikzpicture}\n"
    }
}

internal class FigureNode(val level: Int, val index: Int, val text: ArrayList<String>) {
    val width = text.maxBy { s -> s.length }!!.length
    val height: Int get() = text.size + 1
}
