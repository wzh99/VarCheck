package wzh.llvm.analysis

import wzh.llvm.lang.*
import java.util.*

// Check invalid use of variables, including:
// Using a register variable without first defining it.
// Loading from a pointer to stack variable without first storing to it.
class VarChecker {

    fun check(module: Module) {
        println("Checking ${module.name}")
        module.func.forEach(this::checkFunc)
        println("Finished")
    }

    private lateinit var scope: Scope

    // Register variables that have been defined
    private lateinit var defined: HashMap<BasicBlock, DefinedList>

    // Stack variable pointers that have been stored
    private lateinit var stored: HashMap<BasicBlock, StoredList>

    private fun checkFunc(func: Func) {
        // Initialize data flow values
        scope = func.scope
        val setSize = scope.size
        defined = func.associateWith { block ->
            if (block == func.entry) {
                val entryVal = BitVector(setSize)
                func.param.forEach { p -> entryVal[scope[p]] = true }
                DefinedList(block, setSize, scope, inVal = entryVal)
            } else
                DefinedList(block, setSize, scope)
        } as HashMap
        stored = func.associateWith { block ->
            StoredList(block, setSize, scope)
        } as HashMap

        // Run worklist algorithm
        val work = ArrayDeque<BasicBlock>()
        work.add(func.entry)
        while (work.size > 0) {
            val block = work.pollFirst()
            val defFlow = defined[block]!!
            val prevDefOut = defFlow.outVal.clone()
            defFlow.converge(defined)
            defFlow.transfer()
            val stFlow = stored[block]!!
            val prevStOut = stFlow.outVal.clone()
            stFlow.converge(stored)
            stFlow.transfer()
            if (prevDefOut == defFlow.outVal && prevStOut == stFlow.outVal)
                continue
            block.succ.forEach { succ -> work.addLast(succ) }
        }

        // Check invalid use of variables
        func.forEach { block ->
            val defFlow = defined[block]!!
            val stFlow = stored[block]!!
            block.forEachIndexed { i, inst ->
                inst.use.forEach { use ->
                    if (!defFlow.values[i][scope[use]])
                        report(block, inst, use, "used without defined before")
                }
                if (inst is Load && !stFlow.values[i][scope[inst.src]])
                    report(block, inst, inst.src, "loaded without stored before")
            }
        }
    }

    private fun report(block: BasicBlock, inst: Instruction, sym: Symbol, msg: String) {
        println("Block $block, instruction $inst: Variable $sym $msg.")
    }
}

private class DefinedList(block: BasicBlock, setSize: Int, private val scope: Scope,
                          inVal: BitVector = BitVector(setSize))
    : Flow(block, setSize, inVal, outVal = !BitVector(setSize)) {

    override fun transfer() {
        block.inst.forEachIndexed { i, inst ->
            val next = values[i].clone()
            if (inst.def != null)
                next[scope[inst.def!!]] = true
            values[i + 1] = next
        }
    }

    override val meet = BitVector::and
    override val initial = !BitVector(setSize)
}

private class StoredList(block: BasicBlock, setSize: Int, private val scope: Scope)
    : Flow(block, setSize, inVal = BitVector(setSize), outVal = !BitVector(setSize)) {

    override fun transfer() {
        block.inst.forEachIndexed { i, inst ->
            val next = values[i].clone()
            if (inst is Store)
                next[scope[inst.dst]] = true
            values[i + 1] = next
        }
    }

    override val meet = BitVector::and
    override val initial = !BitVector(setSize)
}
