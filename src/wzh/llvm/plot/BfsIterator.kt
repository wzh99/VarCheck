package wzh.llvm.plot

import wzh.llvm.lang.BasicBlock
import wzh.llvm.lang.Func
import java.util.*
import kotlin.collections.HashSet
import kotlin.collections.Iterator
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty

internal class BfsIterator(func: Func) : Iterator<BlockLevel> {

    private val queue = ArrayDeque<BlockLevel>()
    private val visited = HashSet<BasicBlock>()

    init {
        queue.addLast(BlockLevel(0, func.entry))
        visited.add(func.entry)
    }

    override fun hasNext(): Boolean {
        return queue.isNotEmpty()
    }

    override fun next(): BlockLevel {
        val elem = queue.removeFirst()
        elem.block.succ.forEach { succ ->
            if (!visited.contains(succ)) {
                queue.addLast(BlockLevel(elem.level + 1, succ))
                visited.add(succ)
            }
        }
        return elem
    }
}

internal class BlockLevel(val level: Int, val block: BasicBlock)
