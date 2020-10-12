package net.adriantodt.utils

fun <T> T.comparing(other: T, function: Comparing<T>.() -> Unit): Int {
    return Comparing(this, other)(function)
}

class Comparing<T>(private val that: T, private val other: T) {
    private var result = 0

    operator fun invoke(function: Comparing<T>.() -> Unit): Int {
        function()
        return result
    }

    fun <U : Comparable<U>> reversedBy(block: (T) -> U) {
        if (result == 0) result = block(other).compareTo(block(that))
    }

    fun <U : Comparable<U>> by(block: (T) -> U) {
        if (result == 0) result = block(that).compareTo(block(other))
    }

    private fun <U : Comparable<U?>> nullsByImpl(
        a: U?, b: U?, i: Int = 1, c: Comparator<U> = naturalOrder()
    ): Int {
        return when {
            a === b -> 0
            a == null -> i
            b == null -> -i
            else -> c.compare(a, b)
        }
    }

    fun <U : Comparable<U?>> nullsLastBy(block: (T) -> U?) {
        if (result == 0) result = nullsByImpl(block(that), block(other))
    }

    fun <U : Comparable<U?>> nullsLastReversedBy(block: (T) -> U?) {
        if (result == 0) result = nullsByImpl(block(that), block(other), c = reverseOrder())
    }

    fun <U : Comparable<U?>> nullsFirstBy(block: (T) -> U?) {
        if (result == 0) result = nullsByImpl(block(that), block(other), -1)
    }

    fun <U : Comparable<U?>> nullsFirstReversedBy(block: (T) -> U?) {
        if (result == 0) result = nullsByImpl(block(that), block(other), -1, reverseOrder())
    }
}
