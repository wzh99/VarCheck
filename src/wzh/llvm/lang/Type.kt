package wzh.llvm.lang

abstract class Type {
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
    abstract override fun toString(): String
}

class IntegerType(val bit: Int) : Type() {
    override fun equals(other: Any?): Boolean {
        if (other !is IntegerType) return false
        return bit == other.bit
    }

    override fun hashCode() = bit.hashCode()

    override fun toString() = "i$bit"
}

class PointerType(val target: Type) : Type() {
    override fun equals(other: Any?): Boolean {
        if (other !is PointerType) return false
        return target == other.target
    }

    override fun hashCode() = target.hashCode()

    override fun toString() = "${target}*"
}

interface Typed {
    val type: Type
}
