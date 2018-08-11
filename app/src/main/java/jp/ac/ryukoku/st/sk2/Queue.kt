package jp.ac.ryukoku.st.sk2

class Queue <T>(list: MutableList<T>, size: Int = 100) {
    private var items: MutableList<T> = list
    private val maxsize = size

    fun isEmpty():Boolean = items.isEmpty()
    fun count():Int = items.count()
    fun getItem(postion: Int):T = items[postion]
    fun getList(): MutableList<T> = items

    override fun toString() = items.toString()

    fun push(element: T){
        if (element != null) {
            items.add(0, element)
            if (count() > maxsize) {
                pop()
            }
        }
    }
    fun pop():T?{
        if (isEmpty()){
            return null
        } else {
            return items.removeAt(items.lastIndex)
        }
    }
    fun peek():T?{
        return items.last()
    }
}