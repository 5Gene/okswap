package osp.sparkj.more

interface PackBox {

    /**
     * Accumulates a value starting with [initial] and applying [operation] to the current value
     * and each element from outside in.
     *
     * Elements wrap one another in a chain from left to right; an [Stuff] that appears to the
     * left of another in a `+` expression or in [operation]'s parameter order affects all
     * of the elements that appear after it. [foldIn] may be used to accumulate a value starting
     * from the parent or head of the PackBox chain to the final wrapped child.
     */
    fun <R> foldIn(initial: R, operation: (R, Stuff) -> R): R

    /**
     * Accumulates a value starting with [initial] and applying [operation] to the current value
     * and each element from inside out.
     *
     * Elements wrap one another in a chain from left to right; an [Stuff] that appears to the
     * left of another in a `+` expression or in [operation]'s parameter order affects all
     * of the elements that appear after it. [foldOut] may be used to accumulate a value starting
     * from the child or tail of the PackBox chain up to the parent or head of the chain.
     */
    fun <R> foldOut(initial: R, operation: (Stuff, R) -> R): R

    /**
     * Returns `true` if [predicate] returns true for any [Stuff] in this [PackBox].
     */
    fun any(predicate: (Stuff) -> Boolean): Boolean

    /**
     * Returns `true` if [predicate] returns true for all [Stuff]s in this [PackBox] or if
     * this [PackBox] contains no [Stuff]s.
     */
    fun all(predicate: (Stuff) -> Boolean): Boolean

    /**
     * Concatenates this PackBox with another.
     *
     * Returns a [PackBox] representing this PackBox followed by [other] in sequence.
     */
    infix fun then(other: PackBox): PackBox =
        if (other === PackBox) this else CombinedPackBox(this, other)

    /**
     * A single element contained within a [PackBox] chain.
     */
    interface Stuff : PackBox {
        override fun <R> foldIn(initial: R, operation: (R, Stuff) -> R): R =
            operation(initial, this)

        override fun <R> foldOut(initial: R, operation: (Stuff, R) -> R): R =
            operation(this, initial)

        override fun any(predicate: (Stuff) -> Boolean): Boolean = predicate(this)

        override fun all(predicate: (Stuff) -> Boolean): Boolean = predicate(this)
    }

    /**
     * The companion object `PackBox` is the empty, default, or starter [PackBox]
     * that contains no [elements][Stuff]. Use it to create a new [PackBox] using
     * PackBox extension factory functions:
     *
     * @sample androidx.compose.ui.samples.PackBoxUsageSample
     *
     * or as the default value for [PackBox] parameters:
     *
     * @sample androidx.compose.ui.samples.PackBoxParameterSample
     */
    // The companion object implements `PackBox` so that it may be used as the start of a
    // PackBox extension factory expression.
    companion object : PackBox {
        override fun <R> foldIn(initial: R, operation: (R, Stuff) -> R): R = initial
        override fun <R> foldOut(initial: R, operation: (Stuff, R) -> R): R = initial
        override fun any(predicate: (Stuff) -> Boolean): Boolean = false
        override fun all(predicate: (Stuff) -> Boolean): Boolean = true
        override infix fun then(other: PackBox): PackBox = other
        override fun toString() = "PackBox"
    }
}

/**
 * A node in a [PackBox] chain. A CombinedPackBox always contains at least two elements;
 * a PackBox [outer] that wraps around the PackBox [inner].
 */
class CombinedPackBox(
    internal val outer: PackBox,
    internal val inner: PackBox
) : PackBox {
    override fun <R> foldIn(initial: R, operation: (R, PackBox.Stuff) -> R): R =
        inner.foldIn(outer.foldIn(initial, operation), operation)

    override fun <R> foldOut(initial: R, operation: (PackBox.Stuff, R) -> R): R =
        outer.foldOut(inner.foldOut(initial, operation), operation)

    override fun any(predicate: (PackBox.Stuff) -> Boolean): Boolean =
        outer.any(predicate) || inner.any(predicate)

    override fun all(predicate: (PackBox.Stuff) -> Boolean): Boolean =
        outer.all(predicate) && inner.all(predicate)

    override fun equals(other: Any?): Boolean =
        other is CombinedPackBox && outer == other.outer && inner == other.inner

    override fun hashCode(): Int = outer.hashCode() + 31 * inner.hashCode()

    override fun toString() = "[" + foldIn("") { acc, element ->
        if (acc.isEmpty()) element.toString() else "$acc, $element"
    } + "]"
}