package wzh.varcheck.parse

class Location (val line: Int, val column: Int) : Cloneable {

    constructor() : this(0, 0 )

    internal fun newLine(): Location {
        return Location(line + 1, 0)
    }

    internal fun shift(): Location {
        return Location(line, column + 1)
    }

    override fun toString(): String {
        return "Location{$line, $column}"
    }

}
