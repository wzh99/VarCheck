package wzh.llvm.parse

class ParseError internal constructor(val loc: Location, msg: String)
    : RuntimeException("Line ${loc.line}, column ${loc.column}: $msg")
