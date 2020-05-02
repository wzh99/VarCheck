package wzh.varcheck.parse

open class AstNode(val loc: Location)

internal class ModuleDef(val func: ArrayList<FuncDef>) : AstNode(Location())

internal class FuncDef(loc: Location, val sig: FuncSig, val body: FuncBody) : AstNode(loc)

internal class FuncSig(loc: Location, val ret: TypeDecl, val name: Token, val param: ParamList)
    : AstNode(loc)

internal class ParamList(loc: Location, val list: ArrayList<TypedOperand>) : AstNode(loc)

internal class FuncBody(loc: Location, val blocks: ArrayList<BlockDef>) : AstNode(loc)

internal class BlockDef(loc: Location, val label: Token, val inst: ArrayList<InstDef>) : AstNode(loc)

internal open class InstDef(loc: Location) : AstNode(loc)

internal class AssignInst(loc: Location, val lhs: Token, val rhs: RhsExpr) : InstDef(loc)

internal open class RhsExpr(loc: Location) : AstNode(loc)

internal class AllocaExpr(loc: Location, val type: TypeDecl) : RhsExpr(loc)

internal class LoadExpr(loc: Location, val type: TypeDecl, val src: TypedOperand) : RhsExpr(loc)

internal class BinaryExpr(loc: Location, val op: Token, val type: PrimTypeDecl, val lhs: Token,
                          val rhs: Token)
    : RhsExpr(loc)

internal class ICmpExpr(loc: Location, val op: Token, val type: PrimTypeDecl, val lhs: Token,
                        val rhs: Token)
    : RhsExpr(loc)

internal class CallExpr(loc: Location, val ret: PrimTypeDecl, val func: Token,
                        val args: ArrayList<TypedOperand>)
    : RhsExpr(loc)

internal class StoreInst(loc: Location, val src: TypedOperand, val dst: TypedOperand) : InstDef(loc)

internal class BrInst(loc: Location, val cond: TypedOperand?, val tr: Token, val fls: Token?)
    : InstDef(loc)

internal class RetInst(loc: Location, val value: TypedOperand) : InstDef(loc)

internal class TypedOperand(loc: Location, val type: TypeDecl, val value: Token) : AstNode(loc)

internal open class TypeDecl(loc: Location) : AstNode(loc)

internal class PrimTypeDecl(loc: Location, val name: Token) : TypeDecl(loc)

internal class PtrTypeDecl(loc: Location, val target: TypeDecl) : TypeDecl(loc)
