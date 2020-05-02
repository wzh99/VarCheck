package wzh.varcheck.parse

class Parser(private val lexer: Lexer) {

    private val buf = ArrayList<Token>()
    private var loc = Location()

    // Parse program into AST
    fun parseModule(): AstNode {
        val func = ArrayList<FuncDef>()
        loop@ while (true) {
            when (peek(0)) {
                Token(TokenTag.RESERVED, "define") -> func.add(parseFuncDef())
                Token(TokenTag.EOF) -> break@loop
                else -> error(
                        arrayOf(Token(TokenTag.RESERVED, "define"), Token(TokenTag.EOF)),
                        peek(0)
                )
            }
        }
        return ModuleDef(func)
    }

    private fun parseFuncDef(): FuncDef {
        val loc = loc
        val sig = parseFuncSig()
        val body = parseFuncBody()
        return FuncDef(loc, sig, body)
    }

    private fun parseFuncSig(): FuncSig {
        val loc = loc
        expect(Token(TokenTag.RESERVED, "define"))
        val ret = parseTypeDef()
        val name = expect(TokenTag.GLOBAL_ID)
        val param = parseParamList()
        expect(TokenTag.GROUP_NUM) // don't care attributes of function
        return FuncSig(loc, ret, name, param)
    }

    private fun parseParamList(): ParamList {
        val loc = loc
        expect(TokenTag.LEFT_ROUND)
        val list = ArrayList<TypedOperand>()
        loop@ while (true) {
            when (peek(0).tag) {
                TokenTag.RIGHT_ROUND -> {
                    read()
                    break@loop
                }
                TokenTag.RESERVED -> list.add(parseTypedOperand())
                TokenTag.COMMA -> if (list.isEmpty())
                    throw error(arrayOf(TokenTag.RIGHT_ROUND, TokenTag.RESERVED), peek(0))
                else {
                    read()
                    list.add(parseTypedOperand())
                }
                else -> {
                    var expected = arrayOf(TokenTag.RIGHT_ROUND, TokenTag.RESERVED)
                    if (list.isNotEmpty()) expected += TokenTag.COMMA
                    throw error(expected, peek(0))
                }
            }
        }
        return ParamList(loc, list)
    }

    private fun parseFuncBody(): FuncBody {
        // Begin function body
        val loc = loc
        expect(TokenTag.LEFT_CURLY)

        // Parse all basic blocks
        val block = ArrayList<BlockDef>()
        loop@ while (true) {
            when (peek(0).tag) {
                TokenTag.RESERVED -> block.add(parseBlockDef())
                TokenTag.RIGHT_CURLY -> if (block.isEmpty())
                    throw error(arrayOf(TokenTag.RESERVED), peek(0))
                else
                    break@loop
                else -> {
                    var expected = arrayOf(TokenTag.RESERVED)
                    if (block.isNotEmpty()) expected += TokenTag.RIGHT_CURLY
                    throw error(expected, peek(0))
                }
            }
        }

        // End function body
        expect(TokenTag.RIGHT_CURLY)
        return FuncBody(loc, block)
    }

    private fun parseBlockDef(): BlockDef {
        // Parse label
        val loc = loc
        val label = expect(TokenTag.RESERVED)
        expect(TokenTag.COLON)

        // Parse instructions inside basic block
        val inst = ArrayList<InstDef>()
        loop@ while (true) {
            when (peek(0).tag) {
                TokenTag.LOCAL_ID -> inst.add(parseInstDef())
                TokenTag.RESERVED -> if (peek(1).tag == TokenTag.COLON)
                    break@loop // a new block encountered
                else
                    inst.add(parseInstDef()) // another instruction in this block
                TokenTag.RIGHT_CURLY -> if (inst.isEmpty())
                    throw error(arrayOf(TokenTag.LOCAL_ID, TokenTag.RESERVED), peek(0))
                else
                    break@loop // end of this function
                else -> {
                    var expected = arrayOf(TokenTag.LOCAL_ID, TokenTag.RESERVED)
                    if (inst.isNotEmpty()) expected += TokenTag.RIGHT_CURLY
                    throw error(expected, peek(0))
                }
            }
        }

        return BlockDef(loc, label, inst)
    }

    private fun parseInstDef(): InstDef {
        return when (peek(0).tag) {
            TokenTag.LOCAL_ID -> parseAssignInst()
            TokenTag.RESERVED -> when (peek(0).str) {
                "br" -> parseBrInst()
                "ret" -> parseRetInst()
                "store" -> parseStoreInst()
                else -> throw ParseError(peek(0).loc,
                        "Unrecognized instruction ${peek(0).str}."
                )
            }
            else -> throw error(arrayOf(TokenTag.LOCAL_ID, TokenTag.RESERVED), peek(0))
        }
    }

    private fun parseAssignInst(): AssignInst {
        val loc = loc
        val lhs = expect(TokenTag.LOCAL_ID)
        expect(TokenTag.EQUAL)
        val rhs = parseRhsExpr()
        return AssignInst(loc, lhs, rhs)
    }

    private fun parseRhsExpr(): RhsExpr {
        if (peek(0).tag != TokenTag.RESERVED)
            throw error(arrayOf(TokenTag.RESERVED), peek(0))
        return when (peek(0).str) {
            "alloca" -> parseAllocaExpr()
            "load" -> parseLoadExpr()
            "call" -> parseCallExpr()
            "icmp" -> parseICmpExpr()
            else -> parseBinaryExpr()
        }
    }

    private fun parseAllocaExpr(): AllocaExpr {
        val loc = loc
        read() // `alloca`
        val type = parsePrimType() // only primitive type is considered here
        expect(TokenTag.COMMA)
        expect(Token(TokenTag.RESERVED, "align")) // just scan, alignment is not needed
        expect(TokenTag.DIGITS)
        return AllocaExpr(loc, type)
    }

    private fun parseLoadExpr(): LoadExpr {
        val loc = loc
        read() // `load`
        val type = parsePrimType()
        expect(TokenTag.COMMA)
        val src = parseTypedOperand()
        expect(TokenTag.COMMA)
        expect(Token(TokenTag.RESERVED, "align"))
        expect(TokenTag.DIGITS)
        return LoadExpr(loc, type, src)
    }

    private fun parseCallExpr(): CallExpr {
        // Parse function name and return type
        val loc = loc
        read() // `call`
        val ret = parsePrimType()
        val func = expect(TokenTag.GLOBAL_ID)

        // Parse arguments
        expect(TokenTag.LEFT_ROUND)
        val arg = ArrayList<TypedOperand>()
        loop@ while (true) {
            when (peek(0).tag) {
                TokenTag.RESERVED -> arg.add(parseTypedOperand())
                TokenTag.COMMA -> if (arg.isEmpty())
                    throw error(arrayOf(TokenTag.RESERVED, TokenTag.RIGHT_ROUND), peek(0))
                else {
                    read()
                    arg.add(parseTypedOperand())
                }
                TokenTag.RIGHT_ROUND -> break@loop
                else -> {
                    var expected = arrayOf(TokenTag.RESERVED, TokenTag.RIGHT_ROUND)
                    if (arg.isNotEmpty()) expected += TokenTag.COMMA
                    throw error(expected, peek(0))
                }
            }
        }
        expect(TokenTag.RIGHT_ROUND)

        return CallExpr(loc, ret, func, arg)
    }

    private fun parseICmpExpr(): ICmpExpr {
        val loc = loc
        read() // `icmp`
        val op = expect(TokenTag.RESERVED)
        val type = parsePrimType()
        val lhs = readOperand()
        expect(TokenTag.COMMA)
        val rhs = readOperand()
        return ICmpExpr(loc, op, type, lhs, rhs)
    }

    private fun parseBinaryExpr(): BinaryExpr {
        val loc = loc
        val op = read()
        expect(TokenTag.RESERVED) // `nsw` / `nuw`
        val type = parsePrimType()
        val lhs = readOperand()
        expect(TokenTag.COMMA)
        val rhs = readOperand()
        return BinaryExpr(loc, op, type, lhs, rhs)
    }

    private fun parseRetInst(): RetInst {
        val loc = loc
        read()
        val value = parseTypedOperand()
        return RetInst(loc, value)
    }

    private fun parseBrInst(): BrInst {
        val loc = loc
        read() // `br`
        if (peek(0).tag != TokenTag.RESERVED)
            throw error(arrayOf(TokenTag.RESERVED), peek(0))
        return if (peek(0).strNoPrefix == "label") { // direct jump
            read() // `label`
            val label = expect(TokenTag.LOCAL_ID)
            BrInst(loc, null, label, null)
        } else { // conditional branch
            val cond = parseTypedOperand()
            expect(TokenTag.COMMA)
            expect(Token(TokenTag.RESERVED, "label"))
            val trueLabel = expect(TokenTag.LOCAL_ID)
            expect(TokenTag.COMMA)
            expect(Token(TokenTag.RESERVED, "label"))
            val falseLabel = expect(TokenTag.LOCAL_ID)
            BrInst(loc, cond, trueLabel, falseLabel)
        }
    }

    private fun parseStoreInst(): StoreInst {
        val loc = loc
        read() // `store`
        val src = parseTypedOperand()
        expect(TokenTag.COMMA)
        val dst = parseTypedOperand()
        expect(TokenTag.COMMA)
        expect(Token(TokenTag.RESERVED, "align"))
        expect(TokenTag.DIGITS)
        return StoreInst(loc, src, dst)
    }

    private fun parseTypedOperand(): TypedOperand {
        val loc = loc
        val type = parseTypeDef()
        val value = readOperand()
        return TypedOperand(loc, type, value)
    }

    private fun parseTypeDef(): TypeDecl {
        if (peek(0).tag == TokenTag.RESERVED) { // primitive types appear as reserved words
            val type = parsePrimType()
            return if (peek(0).tag == TokenTag.ASTERISK) parsePtrType(type) else type
        } else
            throw error(arrayOf(TokenTag.RESERVED), peek(0))
    }

    private fun parsePrimType(): PrimTypeDecl {
        val loc = loc
        val name = read()
        return PrimTypeDecl(loc, name)
    }

    private fun parsePtrType(target: TypeDecl): PtrTypeDecl {
        val loc = target.loc
        expect(TokenTag.ASTERISK)
        return PtrTypeDecl(loc, target)
    }

    private fun readOperand(): Token {
        val tok = read()
        if (!tok.isOperand())
            throw error(arrayOf(TokenTag.LOCAL_ID, TokenTag.DIGITS), tok)
        return tok
    }

    // Read one token and check whether the token is expected.
    // The token will not be returned since it is fully anticipated.
    private fun expect(expected: Token) {
        val got = read()
        if (got != expected) throw error(arrayOf(expected), got)
    }

    // Read one token and check whether its tag is expected.
    private fun expect(tag: TokenTag): Token {
        val got = read()
        if (got.tag != tag) throw error(arrayOf(Token(tag)), got)
        return got
    }

    // Read one token from stream, move cursor one step.
    private fun read(): Token {
        val tok = if (buf.isEmpty()) lexer.next() else buf.removeAt(0)
        loc = peek(0).loc
        return tok
    }

    // Look ahead a token of a given index in the stream.
    private fun peek(idx: Int): Token {
        if (idx >= buf.size)
            for (k in 0..idx - buf.size) buf.add(lexer.next())
        return buf[idx]
    }

    companion object {
        // Report parse error by raising an exception
        private fun error(expect: Array<Token>, got: Token): ParseError {
            val expectStr = ArrayList<String>()
            expect.mapTo(expectStr, Companion::tokenString)
            return ParseError(got.loc, "Expect ${expectStr}, got ${tokenString(got)}.")
        }

        private fun error(expected: Array<TokenTag>, got: Token): ParseError {
            val array = Array(expected.size) { i -> Token(expected[i]) }
            return error(array, got)
        }

        // Translate expected token to its readable output string
        private fun tokenString(tok: Token): String {
            return when (tok.tag) {
                TokenTag.RESERVED ->
                    if (tok.str.isEmpty()) TokenTag.RESERVED.toString() else tok.str
                else -> tok.tag.toString()
            }
        }
    }
}
