package wzh.varcheck.parse

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets

class Lexer(path: String) {

    private val src: String
    private var ptr = 0
    private var loc = Location()
    private lateinit var buf: StringBuilder
    private var tag: TokenTag? = null

    init {
        // Read all characters from file
        val file = File(path)
        val ifs = FileInputStream(file)
        val len = ifs.available()
        val bytes = ByteArray(len)
        val read = ifs.read(bytes)
        if (read == -1) throw IOException("No characters can be read from file")
        ifs.close()
        src = String(bytes, StandardCharsets.UTF_8)
    }

    internal fun nextToken(): Token {
        // Initialize fields for lexing
        buf = StringBuilder()
        tag = null

        // Iterate until all characters are consumed
        loop@ while (ptr < src.length) {
            val c = peek()
            when (tag) {
                null -> { // beginning of new token
                    if (isAlphaMark(c)) {
                        tag = TokenTag.RESERVED
                        continue@loop
                    } else if (Character.isDigit(c)) {
                        tag = TokenTag.DIGITS
                        continue@loop
                    }
                    when (c) {
                        '@' -> { // global identifier
                            shift()
                            if (!isAlphaDigitMark(peek())) error(ALPHA_DIGIT_MARK)
                            shift()
                            tag = TokenTag.GLOBAL_ID
                        }
                        '%' -> { // local identifier
                            shift()
                            if (!isAlphaDigitMark(peek())) error(ALPHA_DIGIT_MARK)
                            shift()
                            tag = TokenTag.LOCAL_ID
                        }
                        '#' -> { // attribute group number
                            shift()
                            if (!Character.isDigit(peek())) error(DIGIT)
                            shift()
                            tag = TokenTag.GROUP_NUM
                        }
                        '-' -> { // negative digits
                            shift()
                            if (!Character.isDigit(peek())) error(DIGIT)
                            tag = TokenTag.DIGITS
                        }
                        ';' -> { // begin comment
                            skip()
                            while (read() != '\n') continue // until new line
                        }
                        '=' -> return createFromTag(TokenTag.EQUAL)
                        ',' -> return createFromTag(TokenTag.COMMA)
                        ':' -> return createFromTag(TokenTag.COLON)
                        '*' -> return createFromTag(TokenTag.ASTERISK)
                        '(' -> return createFromTag(TokenTag.LEFT_ROUND)
                        ')' -> return createFromTag(TokenTag.RIGHT_ROUND)
                        '{' -> return createFromTag(TokenTag.LEFT_CURLY)
                        '}' -> return createFromTag(TokenTag.RIGHT_CURLY)
                        '\n' -> {
                            val tok = createFromTag(TokenTag.NEW_LINE)
                            if (tok.loc.column == 0) { // empty line
                                clear()
                                continue@loop
                            } else
                                return tok
                        }
                        ' ', '\r', '\t' -> {
                            skip()
                            continue@loop
                        }
                        else -> throw ParseError(loc, "Unknown character: $c")
                    }
                }
                TokenTag.RESERVED -> {
                    if (isAlphaDigitMark(c)) {
                        shift()
                        continue@loop
                    }
                    // Handle reserved words to ignore code that we don't care
                    val tok = createFromBuffer()
                    when (tok.str) {
                        "target" -> {
                            while (read() != '\n') continue // ignore the whole line
                            clear()
                            continue@loop
                        }
                        "attributes" -> {
                            clear()
                            ptr = src.length // ignore all the code after `attributes`
                        }
                        else -> return tok
                    }
                }
                TokenTag.GLOBAL_ID, TokenTag.LOCAL_ID -> {
                    if (isAlphaDigitMark(c)) shift() else return createFromBuffer()
                }
                TokenTag.DIGITS, TokenTag.GROUP_NUM -> {
                    if (Character.isDigit(c)) shift() else return createFromBuffer()
                }
                else -> throw RuntimeException("Unreachable")
            }
        }

        // Create final token
        return if (buf.isEmpty()) Token(loc, TokenTag.EOF) else createFromBuffer()
    }

    // Create a token from buffer content
    private fun createFromBuffer(): Token {
        val str = buf.toString()
        buf.clear()
        return Token(Location(loc.line, loc.column - str.length), tag!!, str)
    }

    // Create a token from token tag
    private fun createFromTag(tag: TokenTag): Token {
        val tok = Token(loc, tag)
        skip()
        return tok
    }

    // Clear buffer and status
    private fun clear() {
        buf.clear()
        tag = null
    }

    // Read one character to buffer
    private fun shift() { buf.append(read()) }

    // Skip this character without reading it to buffer
    private fun skip() { read() }

    // Read the character currently pointed to. Move the pointer one character forward.
    private fun read(): Char {
        val c = src[ptr++]
        loc = if (c == '\n') loc.newLine() else loc.shift()
        return c
    }

    // Look ahead one character in the source. If EOF is reached, return 0
    private fun peek(): Char {
        return if (ptr < src.length) src[ptr] else '\u0000'
    }

    // Report lexing error by raising an exception
    @Throws(ParseError::class)
    private fun error(expect: String) {
        throw ParseError(loc, "Expect $expect, got ${peek()}.")
    }

    companion object {
        private const val DIGIT = "[0-9]"
        private const val ALPHA_DIGIT_MARK = "[A-Za-z0-9._]"
        private fun isMark(c: Char): Boolean {
            return c == '.' || c == '_'
        }

        private fun isAlphaMark(c: Char): Boolean {
            return isMark(c) || Character.isUpperCase(c) || Character.isLowerCase(c)
        }

        private fun isAlphaDigitMark(c: Char): Boolean {
            return isAlphaMark(c) || Character.isDigit(c)
        }
    }
}
