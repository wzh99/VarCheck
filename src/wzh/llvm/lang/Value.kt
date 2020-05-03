package wzh.llvm.lang

abstract class Value : Typed {
    abstract override val type: Type
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
    abstract override fun toString(): String
}

class Symbol(override val type: Type, val name: String) : Value() {
    override fun equals(other: Any?): Boolean {
        if (other !is Symbol) return false
        return name == other.name
    }

    override fun hashCode(): Int { return name.hashCode() }

    override fun toString(): String { return "%$name" }
}

abstract class Constant(override val type: Type) : Value()

class I32Const(val value: Int) : Constant(IntegerType(32)) {
    override fun equals(other: Any?): Boolean {
        if (other !is I32Const) return false
        return value == other.value
    }

    override fun hashCode(): Int { return value.hashCode() }

    override fun toString(): String { return value.toString() }
}

