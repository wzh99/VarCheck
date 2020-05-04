package wzh.llvm.parse

class Location(val line: Int, val column: Int) : Cloneable {

    constructor() : this(0, 0)

    internal fun newLine() = Location(line + 1, 0)

    internal fun shift() = Location(line, column + 1)

    override fun toString() = "Location{$line, $column}"

}
