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
        return Module(func)
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
        val list = ArrayList<ParamDef>()
        loop@ while (true) {
            when (peek(0).tag) {
                TokenTag.RIGHT_ROUND -> {
                    read()
                    break@loop
                }
                TokenTag.RESERVED -> list.add(parseParamDef())
                TokenTag.COMMA -> if (list.size > 0) {
                    read()
                    list.add(parseParamDef())
                } else throw error(
                        arrayOf(Token(TokenTag.RIGHT_ROUND), Token(TokenTag.RESERVED)),
                        peek(0)
                )
                else -> {
                    var expected = arrayOf(Token(TokenTag.RIGHT_ROUND),
                            Token(TokenTag.RESERVED))
                    if (list.size > 0) expected += Token(TokenTag.COMMA)
                    throw error(expected, peek(0))
                }
            }
        }
        return ParamList(loc, list)
    }

    private fun parseParamDef(): ParamDef {
        val loc = loc
        val type = parseTypeDef()
        val name = expect(TokenTag.LOCAL_ID)
        return ParamDef(loc, type, name)
    }

    private fun parseFuncBody(): FuncBody {
        throw RuntimeException()
    }

    private fun parseTypeDef(): TypeDef {
        if (peek(0).tag == TokenTag.RESERVED) { // primitive types appear as reserved words
            val type = parsePrimType()
            return if (peek(0).tag == TokenTag.ASTERISK) parsePtrType(type) else type
        } else
            throw error(arrayOf(Token(TokenTag.RESERVED)), peek(0))
    }

    private fun parsePrimType(): PrimType {
        val loc = loc
        val name = read()
        return PrimType(loc, name)
    }

    private fun parsePtrType(target: TypeDef): PtrType {
        val loc = target.loc
        expect(Token(TokenTag.ASTERISK))
        return PtrType(loc, target)
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
        val tok = if (buf.isEmpty()) lexer.nextToken() else buf.removeAt(0)
        loc = peek(0).loc
        return tok
    }

    // Look ahead a token of a given index in the stream.
    private fun peek(idx: Int): Token {
        if (idx >= buf.size)
            for (k in 0..idx - buf.size) buf.add(lexer.nextToken())
        return buf[idx]
    }

    companion object {
        // Report parse error by raising an exception
        private fun error(expect: Array<Token>, got: Token): ParseError {
            val expectStr = ArrayList<String>()
            expect.mapTo(expectStr, Companion::tokenString)
            return ParseError(got.loc, "Expect ${expectStr}, got ${tokenString(got)}.")
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
