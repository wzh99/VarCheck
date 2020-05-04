package wzh.llvm.lang

class BasicBlock(val name: String) {

    val inst: MutableList<Instruction> = ArrayList()
    val pred: MutableList<BasicBlock> = ArrayList()
    val succ: MutableList<BasicBlock> = ArrayList()

    fun connect(to: BasicBlock) {
        if (succ.contains(to)) return
        succ.add(to)
        to.pred.add(this)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BasicBlock) return false
        return name == other.name
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = "%$name"
}
