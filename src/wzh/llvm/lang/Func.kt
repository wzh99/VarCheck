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

    override fun iterator(): Iterator<BasicBlock> {
        return blocks.iterator()
    }

    override fun toString(): String {
        return "$ret @$name(${param.joinToString { p -> "${p.type} $p" }})"
    }
}
