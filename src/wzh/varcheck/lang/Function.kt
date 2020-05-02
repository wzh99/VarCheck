package wzh.varcheck.lang

class Function(val name: String, val ret: Type, val param: List<Symbol>) {

    val blocks: MutableList<BasicBlock> = ArrayList()
    val entry: BasicBlock get() = blocks[0]
    val exit: MutableList<BasicBlock> = ArrayList()

    val scope = Scope()

    init {
        for (p in param) scope.add(p)
    }
}
