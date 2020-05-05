package wzh.llvm.analysis

import wzh.llvm.lang.BasicBlock

abstract class Flow(protected val block: BasicBlock, private val setSize: Int,
                    inVal: BitVector, outVal: BitVector) {
    // Data flow values for each instruction
    val values = Array(block.size + 1) { i ->
        when (i) {
            0 -> inVal
            block.size -> outVal
            else -> BitVector(setSize)
        }
    }.toList() as ArrayList

    var inVal
        get() = values.first()
        set(value) {
            values[0] = value
        }

    var outVal
        get() = values.last()
        set(value) {
            values[block.size] = value
        }

    // Define how values are transferred through the block
    abstract fun transfer()

    // Define meet operator of values
    abstract val meet: (BitVector, BitVector) -> BitVector

    // What is the initial value when applying meet operations
    abstract val initial: BitVector

    // Confluence of values in predecessors
    fun converge(map: Map<BasicBlock, Flow>) {
        if (block.pred.isEmpty()) return // keep original IN value
        inVal = block.pred
                .map { pred -> (map[pred] ?: error("Invalid block-flow map")).outVal }
                .fold(initial, meet)
    }
}
