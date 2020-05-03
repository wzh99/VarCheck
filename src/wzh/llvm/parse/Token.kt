package wzh.llvm.parse


internal class Token(val loc: Location, val tag: TokenTag, val str: String) {

    constructor(loc: Location, tag: TokenTag) : this(loc, tag, "")

    constructor(tag: TokenTag, str: String) : this(Location(), tag, str)

    constructor(tag: TokenTag) : this(Location(), tag)

    override fun toString(): String {
        return "Token{$loc, $tag, $str}"
    }

    fun isOperand(): Boolean {
        return when (tag) {
            TokenTag.LOCAL_ID, TokenTag.DIGITS -> true
            else -> false
        }
    }

    val strNoPrefix
        get() = when (tag) {
            TokenTag.GLOBAL_ID, TokenTag.LOCAL_ID -> str.substring(1)
            else -> str
        }

    override fun equals(other: Any?): Boolean {
        if ((other is Token).not()) return false
        val o = other as Token
        return tag == o.tag && str == o.str
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + str.hashCode()
        return result
    }
}

internal enum class TokenTag {
    RESERVED,  // [A-Za-z._][A-Za-z0-9._]*
    GLOBAL_ID,  // @[A-Za-z0-9._]+
    LOCAL_ID,  // %[A-Za-z0-9._]+
    DIGITS,  // -?[0-9]+
    GROUP_NUM,  // #[0-9]+
    EQUAL,  // =
    COMMA,  // ,
    COLON,  // :
    ASTERISK,  // *
    LEFT_ROUND,  // (
    RIGHT_ROUND,  // )
    LEFT_CURLY,  // {
    RIGHT_CURLY,  // }
    EOF
}
