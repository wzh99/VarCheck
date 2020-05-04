package wzh.llvm.analysis

import wzh.llvm.lang.*
import java.util.*

// Check invalid use of variables, including:
// For a register variable, use without define.
// For a stack variable, load without store.
class VarChecker {

    fun check(module: Module) {
        println("Checking ${module.name}")
        module.func.forEach(this::checkFunc)
    }

    private lateinit var scope: Scope
    private lateinit var defined: HashMap<BasicBlock, DataFlowValue>
    private lateinit var stored: HashMap<BasicBlock, DataFlowValue>

    private fun checkFunc(func: Func) {
        // Initialize data flow values
        scope = func.scope
        val size = scope.size
        defined = func.blocks.associateWith { block ->
            val transfer = BitVector(size)
            setDefined(transfer, block)
            val inVal = BitVector(size)
            if (block == func.entry)
                func.param.forEach { param -> inVal[scope[param]] = true }
            DataFlowValue(size, transfer, inVal)
        } as HashMap
        stored = func.blocks.associateWith { block ->
            val transfer = BitVector(size)
            setStored(transfer, block)
            DataFlowValue(size, transfer)
        } as HashMap

        // Run worklist algorithm
        val work = ArrayDeque(arrayListOf(func.entry))
        while (work.isNotEmpty()) {
            // Record previous data flow values
            val block = work.pollFirst()
            val prevDefOut = defined[block]!!.outVal
            val predStOut = stored[block]!!.outVal

            // Update data flow values in this iteration
            val defVal = defined[block]!!
            val stVal = stored[block]!!
            if (block != func.entry) {
                defVal.inVal = block.pred
                        .map { pred -> defined[pred]!!.outVal }
                        .fold(!BitVector(size), BitVector::and) // meet operator: intersection
                stVal.inVal = block.pred
                        .map { pred -> stored[pred]!!.outVal }
                        .fold(!BitVector(size), BitVector::and)
            }
            defVal.update()
            stVal.update()

            // Add successors to list
            if (prevDefOut == defVal.outVal && predStOut == stVal.outVal) continue
            block.succ.forEach { succ -> work.addLast(succ) }
        }
    }

    private fun setDefined(vec: BitVector, block: BasicBlock) {
        block.inst.forEach { inst ->
            if (inst.def != null) vec[scope[inst.def!!]] = true
        }
    }

    private fun setStored(vec: BitVector, block: BasicBlock) {
        block.inst.forEach { inst ->
            if (inst is Store) vec[scope[inst.dst]]
        }
    }
}

private class DataFlowValue(size: Int, val transfer: BitVector,
                            var inVal: BitVector = BitVector(size)) {
    var outVal = !BitVector(size)

    init {
        if (size != transfer.size)
            throw IllegalArgumentException()
    }

    // Apply transfer function
    fun update() {
        outVal = inVal or transfer
    }
}
