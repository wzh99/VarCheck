package wzh.llvm.lang

class Scope : Iterable<Symbol> {
    private val list = ArrayList<Symbol>()
    private val index = HashMap<Symbol, Int>()
    private val table = HashMap<String, Symbol>()

    val size: Int get() = list.size

    fun contains(name: String): Boolean {
        return table.contains(name)
    }

    // Get symbol of given index
    operator fun get(index: Int): Symbol {
        return list[index]
    }

    // Get index of a given symbol
    operator fun get(symbol: Symbol): Int {
        return index[symbol]!!
    }

    // Get symbol of given name
    operator fun get(name: String): Symbol {
        return table[name]!!
    }

    fun add(symbol: Symbol) {
        if (table.containsKey(symbol.name)) return
        val i = size
        list.add(symbol)
        index[symbol] = i
        table[symbol.name] = symbol
    }

    fun toArray(): Array<Symbol> {
        return Array(list.size) { i -> list[i] }
    }

    override fun iterator(): Iterator<Symbol> {
        return list.iterator()
    }
}
