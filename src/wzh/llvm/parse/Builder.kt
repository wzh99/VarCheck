package wzh.llvm.parse

import wzh.llvm.lang.*

class Builder(node: AstNode) {

    private val ast = node as ModuleDef
    private var ctx: Context? = null

    fun build(): Module {
        val func = ast.func.map(this::visitFunc)
        return Module(ast.name, func)
    }

    private fun visitFunc(funcDef: FuncDef): Func {
        // Build signature
        val sig = funcDef.sig
        val ret = visitType(sig.ret)
        val param = visitParamList(sig.param)
        val func = Func(sig.name.strNoPrefix, ret, param)

        // Map labels to blocks
        val body = funcDef.body
        val blockMap = HashMap<String, BasicBlock>()
        for (b in body.blocks) {
            val bb = BasicBlock(b.label.strNoPrefix)
            func.blocks.add(bb)
            blockMap[bb.name] = bb
        }

        // Build blocks
        ctx = Context(func.scope, blockMap)
        body.blocks.forEach(this::visitBlock)

        return func
    }

    private fun visitParamList(param: ParamList): List<Symbol> {
        return param.list.map { p -> visitTypedOperand(p) as Symbol }
    }

    private fun visitBlock(blockDef: BlockDef) {
        val block = ctx!!.blockMap[blockDef.label.strNoPrefix]!!
        for (inst in blockDef.inst)
            block.inst.add(visitInst(inst, block))
    }

    private fun visitInst(inst: InstDef, block: BasicBlock): Instruction {
        return when (inst) {
            is AssignInst -> visitAssignInst(inst)
            is StoreInst -> visitStoreInst(inst)
            is BrInst -> visitBrInst(inst, block)
            is RetInst -> visitRetInst(inst)
            else -> error("Unreachable")
        }
    }

    private fun visitAssignInst(inst: AssignInst): Instruction {
        val dst = inst.lhs
        return visitRhsExpr(dst, inst.rhs)
    }

    private fun visitRhsExpr(dst: Token, expr: RhsExpr): Instruction {
        return when (expr) {
            is AllocaExpr -> visitAllocaExpr(dst, expr)
            is LoadExpr -> visitLoadExpr(dst, expr)
            is ICmpExpr -> visitICmpExpr(dst, expr)
            is BinaryExpr -> visitBinaryExpr(dst, expr)
            is CallExpr -> visitCallExpr(dst, expr)
            else -> error("Unreachable")
        }
    }

    private fun visitAllocaExpr(dstTok: Token, expr: AllocaExpr): Alloca {
        val type = visitType(expr.type)
        val dst = visitLocalId(dstTok, type)
        return Alloca(dst, type)
    }

    private fun visitLoadExpr(dstTok: Token, expr: LoadExpr): Load {
        val type = visitType(expr.type)
        val dst = visitLocalId(dstTok, type)
        val src = visitTypedOperand(expr.src)
        return Load(dst, type, src)
    }

    private fun visitBinaryExpr(dstTok: Token, expr: BinaryExpr): Binary {
        val type = visitType(expr.type)
        val dst = visitLocalId(dstTok, type)
        val op = expr.op.str
        val lhs = visitOperand(expr.lhs, type)
        val rhs = visitOperand(expr.rhs, type)
        return Binary(dst, op, type, lhs, rhs)
    }

    private fun visitICmpExpr(dstTok: Token, expr: ICmpExpr): ICmp {
        val type = visitType(expr.type)
        val dst = visitLocalId(dstTok, type)
        val op = expr.op.str
        val lhs = visitOperand(expr.lhs, type)
        val rhs = visitOperand(expr.rhs, type)
        return ICmp(dst, op, type, lhs, rhs)
    }

    private fun visitCallExpr(dstTok: Token, expr: CallExpr): Call {
        val type = visitType(expr.ret)
        val dst = visitLocalId(dstTok, type)
        val func = expr.func.strNoPrefix
        val args = expr.args.map(this::visitTypedOperand)
        return Call(dst, type, func, args)
    }

    private fun visitStoreInst(inst: StoreInst): Store {
        val src = visitTypedOperand(inst.src)
        val dst = visitTypedOperand(inst.dst) as Symbol
        return Store(src, dst)
    }

    private fun visitBrInst(inst: BrInst, block: BasicBlock): Br {
        return if (inst.cond == null) { // direct jump
            val target = ctx!!.blockMap[inst.tr.strNoPrefix]!!
            block.connect(target)
            Br(null, target, null)
        } else { // conditional branch
            val cond = visitTypedOperand(inst.cond)
            val tr = ctx!!.blockMap[inst.tr.strNoPrefix]!!
            val fls = ctx!!.blockMap[inst.fls!!.strNoPrefix]!!
            block.connect(tr)
            block.connect(fls)
            Br(cond, tr, fls)
        }
    }

    private fun visitRetInst(inst: RetInst): Ret {
        val value = visitTypedOperand(inst.value)
        return Ret(value)
    }

    private fun visitTypedOperand(opd: TypedOperand): Value {
        return when (opd.value.tag) {
            TokenTag.LOCAL_ID -> visitLocalId(opd.value, visitType(opd.type))
            TokenTag.DIGITS -> visitDigits(opd.value)
            else -> error("Unreachable")
        }
    }

    private fun visitOperand(tok: Token, type: Type): Value {
        return when (tok.tag) {
            TokenTag.LOCAL_ID -> visitLocalId(tok, type)
            TokenTag.DIGITS -> visitDigits(tok)
            else -> error("Unreachable")
        }
    }

    private fun visitLocalId(tok: Token, type: Type): Symbol {
        assert(tok.tag == TokenTag.LOCAL_ID)
        val name = tok.strNoPrefix
        return if (ctx == null || !ctx!!.scope.contains(name)) {
            val sym = Symbol(type, tok.strNoPrefix)
            ctx?.scope?.add(sym)
            sym
        }
        else
            ctx!!.scope[name]
    }

    private fun visitDigits(tok: Token): I32Const {
        assert(tok.tag == TokenTag.DIGITS)
        return I32Const(tok.str.toInt())
    }

    private fun visitType(type: TypeDecl): Type {
        return when (type) {
            is PrimTypeDecl -> reservedToType[type.name.str]
                    ?: throw ParseError(type.loc, "Unknown type ${type.name}.")
            is PtrTypeDecl -> PointerType(visitType(type.target))
            else -> error("Unreachable")
        }
    }

    companion object {
        val reservedToType = mapOf(
                Pair("i1", IntegerType(1)), Pair("i32", IntegerType(32))
        )
    }
}

private class Context(val scope: Scope, val blockMap: HashMap<String, BasicBlock>)
