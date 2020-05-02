package wzh.varcheck.lang

class BasicBlock(val name: String) {

    val inst: MutableList<Instruction> = ArrayList()
    val pred: MutableList<BasicBlock> = ArrayList()
    val succ: MutableList<BasicBlock> = ArrayList()

    fun connect(to: BasicBlock) {
        if (succ.contains(to)) return
        succ.add(to)
        to.pred.add(this)
    }
}
