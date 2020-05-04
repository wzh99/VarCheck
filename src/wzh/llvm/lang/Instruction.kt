package wzh.llvm.lang

abstract class Instruction {
    abstract val use: List<Value>
    abstract val def: Symbol?
    abstract override fun toString(): String
}

class Alloca(val dst: Symbol, val type: Type) : Instruction() {
    override val use: List<Value> get() = arrayListOf()
    override val def: Symbol? get() = dst
    override fun toString() = "$dst = alloca $type"
}

class Load(val dst: Symbol, val type: Type, val src: Value) : Instruction() {
    override val use: List<Value> get() = arrayListOf(src)
    override val def: Symbol? get() = dst
    override fun toString() = "$dst = load $type, ${src.type} $src"
}

class Store(val src: Value, val dst: Symbol) : Instruction() {
    override val use: List<Value> get() = arrayListOf(src)
    override val def: Symbol? get() = dst
    override fun toString() = "store ${src.type} $src, ${dst.type} $dst"
}

class Call(val dst: Symbol, val type: Type, val func: String, val arg: List<Value>)
    : Instruction() {
    override val use: List<Value> get() = arg
    override val def: Symbol? get() = dst
    override fun toString() = "$dst = call $type @$func(${arg.joinToString()})"
}

class Binary(val dst: Symbol, val op: String, val type: Type, val lhs: Value, val rhs: Value)
    : Instruction() {
    override val use: List<Value> get() = arrayListOf(lhs, rhs)
    override val def: Symbol? get() = dst
    override fun toString() = "$dst = $op $type $lhs, $rhs"
}

class ICmp(val dst: Symbol, val op: String, val type: Type, val lhs: Value, val rhs: Value)
    : Instruction() {
    override val use: List<Value> get() = arrayListOf(lhs, rhs)
    override val def: Symbol? get() = dst
    override fun toString() = "$dst = icmp $op $type $lhs, $rhs"
}

class Br(val cond: Value?, val tr: BasicBlock, val fls: BasicBlock?) : Instruction() {
    override val use: List<Value>
        get() = if (cond == null) arrayListOf() else arrayListOf(cond)
    override val def: Symbol? get() = null
    override fun toString() =
            if (cond == null)
                "br label $tr"
            else
                "br ${cond.type} $cond, label $tr, label $fls"
}

class Ret(val value: Value) : Instruction() {
    override val use: List<Value> get() = arrayListOf(value)
    override val def: Symbol? get() = null
    override fun toString() = "ret ${value.type} $value"
}
