package wzh.varcheck.lang

class Scope {
    private val list = ArrayList<Symbol>()
    private val index = HashMap<Symbol, Int>()
    private val table = HashMap<String, Symbol>()

    val size: Int get() = list.size

    fun contains(name: String): Boolean {
        return table.contains(name)
    }

    operator fun get(index: Int): Symbol {
        return list[index]
    }

    operator fun get(symbol: Symbol): Int {
        return index[symbol]!!
    }

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
}
