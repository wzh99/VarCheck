package wzh.llvm.analysis

class BitVector(val size: Int, private val words: LongArray) : Iterable<Boolean> {

    constructor(size: Int) : this(size, LongArray((size - 1) / Long.SIZE_BITS + 1) { 0 })

    operator fun not() = BitVector(size, words.map(Long::inv).toLongArray())

    infix fun or(other: BitVector): BitVector {
        if (size != other.size)
            throw IllegalArgumentException("Size not match")
        return BitVector(size, words.zip(other.words).map { (l, r) -> l or r }.toLongArray())
    }

    infix fun and(other: BitVector): BitVector {
        if (size != other.size)
            throw IllegalArgumentException("Size not match")
        return BitVector(size, words.zip(other.words).map { (l, r) -> l and r }.toLongArray())
    }

    operator fun get(index: Int): Boolean {
        if (index >= size)
            throw IndexOutOfBoundsException()
        val word = words[wordIndex(index)]
        val mask = 1L shl bitIndex(index)
        return (word and mask) != 0L
    }

    operator fun set(index: Int, bit: Boolean) {
        if (index >= size)
            throw IndexOutOfBoundsException()
        val wordIdx = wordIndex(index)
        val word = words[wordIdx]
        val mask = 1L shl bitIndex(index)
        if (bit)  // set that bit
            words[wordIdx] = word or mask
        else
            words[wordIdx] = word and mask.inv()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BitVector) return false
        return toString() == other.toString()
    }

    fun clone() = BitVector(size, words)

    override fun toString() =
            reversed().joinToString(separator = "") { b -> if (b) "1" else "0" }

    override fun iterator() = BitIterator(this)

    private fun wordIndex(index: Int) = index / Long.SIZE_BITS

    private fun bitIndex(index: Int) = index % Long.SIZE_BITS

}

class BitIterator(private val vec: BitVector) : Iterator<Boolean> {
    private var index = 0
    override fun hasNext() = index < vec.size
    override fun next() = vec[index++]
}
