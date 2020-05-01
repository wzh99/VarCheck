package wzh.varcheck.parse

open class AstNode(val loc: Location)

internal class Module(val func: ArrayList<FuncDef>) : AstNode(Location())

internal class FuncDef(loc: Location, val sig: FuncSig, val body: FuncBody) : AstNode(loc)

internal class FuncSig(loc: Location, val ret: TypeDef, val name: Token, val param: ParamList)
    : AstNode(loc)

internal class ParamList(loc: Location, val list: ArrayList<ParamDef>) : AstNode(loc)

internal class ParamDef(loc: Location, val type: TypeDef, val name: Token) : AstNode(loc)

internal class FuncBody(loc: Location, val block: ArrayList<BlockDef>) : AstNode(loc)

internal class BlockDef(loc: Location, val name: Token, val inst: ArrayList<InstDef>) : AstNode(loc)

internal open class InstDef(loc: Location) : AstNode(loc)

internal class AssignInst(loc: Location, val lhs: Token, val rhs: RhsExpr) : InstDef(loc)

internal open class RhsExpr(loc: Location) : AstNode(loc)

internal class AllocaExpr(loc: Location, val type: TypeDef) : RhsExpr(loc)

internal class LoadExpr(loc: Location, val type: TypeDef, val src: TypedOperand) : RhsExpr(loc)

internal class BinaryExpr(loc: Location, val op: Token, val type: TypeDef, val lhs: Operand,
                       val rhs: Operand)
    : RhsExpr(loc)

internal class CmpExpr(loc: Location, val op: Token, val type: TypeDef, val lhs: Operand,
                       val rhs: Operand)
    : RhsExpr(loc)

internal class BrInst(loc: Location, val cond: TypedOperand?, val trLab: Token, val flsLab: Token?)
    : InstDef(loc)

internal class StoreInst(loc: Location, val src: TypedOperand, val dst: TypedOperand) : InstDef(loc)

internal class Operand(loc: Location, val value: Token) : AstNode(loc)

internal class TypedOperand(loc: Location, val type: TypeDef, val value: Token) : AstNode(loc)

internal open class TypeDef(loc: Location) : AstNode(loc)

internal class PrimType(loc: Location, val name: Token) : TypeDef(loc)

internal class PtrType(loc: Location, val target: TypeDef) : TypeDef(loc)
