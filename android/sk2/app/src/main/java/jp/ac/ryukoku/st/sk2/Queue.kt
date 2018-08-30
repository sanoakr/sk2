package jp.ac.ryukoku.st.sk2

/** ////////////////////////////////////////////////////////////////////////////// **/
/*** FIFO Queue: ローカルログ記録用 ***/
class Queue <T>(list: MutableList<T> = mutableListOf(), size: Int = 100) {
    var items: MutableList<T> = list
    val maxsize = size        // Queue の最大長

    fun isEmpty():Boolean = items.isEmpty()
    fun count():Int = items.count()
    fun clear() = items.clear()
    fun get(postion: Int):T = items[postion]
    //fun getItem(postion: Int):T = items[postion]
    override fun toString() = items.toString()
    /** ////////////////////////////////////////////////////////////////////////////// **/
    fun push(element: T){ // 先頭に PUSH
        if (element != null) {
            items.add(0, element)
            if (count() > maxsize) {
                pop()
            }
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    fun pop(): T? { // 最後から POP
        return if (this.isEmpty()){
            null
        } else {
            items.removeAt(items.lastIndex)
        }
    }
    /** ////////////////////////////////////////////////////////////////////////////// **/
    /**fun peek():T?{ // 最後の要素を PEEK (消さない)
        return items.last()
    }**/
    /** ////////////////////////////////////////////////////////////////////////////// **/
}
