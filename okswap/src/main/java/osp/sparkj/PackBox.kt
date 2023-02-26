package osp.sparkj

/**
 *   CombinedPackBox
 * -------------------------------
 * |            --------------   |
 * |            |            |   |
 * |    this    |   other    |   |
 * |  (outer)   |  (inner)   |   |
 * |            |            |   |
 * |            --------------   |
 * -------------------------------
 */
interface PackBox {

    /**
     * 以[initial]为初始值开始 从外到内(头到尾)的每个元素通过应用 [operation] 来完成累积
     *
     * 元素从左到右(外到内)的被包裹再[PackBox]链里面
     * [foldIn] may be used to accumulate a value starting
     * from the parent or head of the PackBox chain to the final wrapped child.
     */
    fun <R> foldIn(initial: R, operation: (R, Stuff) -> R): R

    /**
     * 以[initial]为初始值开始 从内到外(尾到头)的每个元素通过应用 [operation] 来完成累积
     *
     * [foldOut] may be used to accumulate a value starting
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
     * 把这个 PackBox 和其他的连接起来
     *
     * Returns a [PackBox] representing this PackBox followed by [other] in sequence.
     *
     *      CombinedPackBox
     *   -------------------------------
     *   |            --------------   |
     *   |            |            |   |
     *   |    this    |   other    |   |
     *   |  (outer)   |  (inner)   |   |
     *   |            |            |   |
     *   |            --------------   |
     *   -------------------------------
     */
    infix fun then(other: PackBox): PackBox =
        if (other === PackBox) this else CombinedPackBox(this, other)

    operator fun plus(other: PackBox): PackBox {
        val removed = minusKey(other)
        return if (removed == PackBox) {
            this
        } else {
            CombinedPackBox(removed, other)
        }
    }

    /**
     * 链里面除了key之外的其他
     * return PackBox表示就是key
     */
    fun minusKey(key: PackBox): PackBox

    /**
     * [PackBox] 链里面的一个单一的元素
     */
    interface Stuff : PackBox {
        override fun <R> foldIn(initial: R, operation: (R, Stuff) -> R): R =
            operation(initial, this)

        override fun <R> foldOut(initial: R, operation: (Stuff, R) -> R): R =
            operation(this, initial)

        override fun any(predicate: (Stuff) -> Boolean): Boolean = predicate(this)

        override fun all(predicate: (Stuff) -> Boolean): Boolean = predicate(this)

        override fun minusKey(key: PackBox): PackBox {
            val find = any {
                it == key
            }
            return if (find) {
                PackBox
            } else {
                this
            }
        }
    }

    /**
     * [PackBox] 这个伴生对象是空的，或者起点 不包含任何内部元素
     * 用它来创建一个新的 [PackBox] 链
     */
    companion object : PackBox {
        override fun <R> foldIn(initial: R, operation: (R, Stuff) -> R): R = initial
        override fun <R> foldOut(initial: R, operation: (Stuff, R) -> R): R = initial
        override fun any(predicate: (Stuff) -> Boolean): Boolean = false
        override fun all(predicate: (Stuff) -> Boolean): Boolean = true
        override infix fun then(other: PackBox): PackBox = other
        override fun minusKey(key: PackBox): PackBox = this

        override fun toString() = "PackBox"
    }
}

/**
 * 是再[PackBox]链中的一个节点
 * 一个[CombinedPackBox]总是包含两个元素[Stuff]
 * a PackBox [outer] that wraps around the PackBox [inner].
 *
 *       CombinedPackBox
 *    -------------------------------
 *    |            --------------   |
 *    |            |            |   |
 *    |    this    |   other    |   |
 *    |  (outer)   |  (inner)   |   |
 *    |            |            |   |
 *    |            --------------   |
 *    -------------------------------
 *
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

    override fun minusKey(key: PackBox): PackBox {
        var removed = outer.minusKey(key)
        return if (removed == PackBox) {
            removed = inner.minusKey(key)
            if (removed == PackBox) {
                PackBox
            } else {
                inner
            }
        } else {
            removed = inner.minusKey(key)
            if (removed == PackBox) {
                outer
            } else {
                this
            }
        }
    }

    override fun equals(other: Any?): Boolean =
        other is CombinedPackBox && outer == other.outer && inner == other.inner

    override fun hashCode(): Int = outer.hashCode() + 31 * inner.hashCode()

    override fun toString() = "[" + foldIn("") { acc, element ->
        if (acc.isEmpty()) element.toString() else "$acc, $element"
    } + "]"
}