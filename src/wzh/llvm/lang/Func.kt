package wzh.llvm.lang

class Func(val name: String, val ret: Type, val param: List<Symbol>)
    : Iterable<BasicBlock> {

    val blocks: MutableList<BasicBlock> = ArrayList()
    val entry: BasicBlock get() = blocks[0]
    val exit: MutableList<BasicBlock> = ArrayList()

    val scope = Scope()

    init {
        for (p in param) scope.add(p)
    }

    override fun iterator() = blocks.iterator()

    override fun toString() = "$ret @$name(${param.joinToString { p -> "${p.type} $p" }})"
}
