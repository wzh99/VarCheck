package wzh.varcheck.lang

abstract class Instruction {
    abstract val use: List<Value>
    abstract val def: Symbol?
}

class Alloca(val dst: Symbol, val type: Type) : Instruction() {
    override val use: List<Value> get() = arrayListOf()
    override val def: Symbol? get() = dst
}

class Load(val dst: Symbol, val type: Type, val src: Value) : Instruction() {
    override val use: List<Value> get() = arrayListOf(src)
    override val def: Symbol? get() = dst
}

class Store(val src: Value, val dst: Symbol) : Instruction() {
    override val use: List<Value> get() = arrayListOf(src)
    override val def: Symbol? get() = dst
}

class Call(val dst: Symbol, val func: String, val arg: ArrayList<Value>) : Instruction() {
    override val use: List<Value> get() = arg
    override val def: Symbol? get() = dst
}

class Binary(val dst: Symbol, val op: String, val lhs: Value, val rhs: Value) : Instruction() {
    override val use: List<Value> get() = arrayListOf(lhs, rhs)
    override val def: Symbol? get() = dst
}

class ICmp(val dst: Symbol, val op: String, val lhs: Value, val rhs: Value) : Instruction() {
    override val use: List<Value> get() = arrayListOf(lhs, rhs)
    override val def: Symbol? get() = dst
}

class Br(val cond: Value?, val tr: BasicBlock, val fls: BasicBlock?) : Instruction() {
    override val use: List<Value>
        get() = if (cond == null) arrayListOf() else arrayListOf(cond)
    override val def: Symbol? get() = null
}

class Ret(val value: Value) : Instruction() {
    override val use: List<Value> get() = arrayListOf(value)
    override val def: Symbol? get() = null
}
