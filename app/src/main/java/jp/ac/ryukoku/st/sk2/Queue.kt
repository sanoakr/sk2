package jp.ac.ryukoku.st.sk2

class Queue <T>(list: MutableList<T>, size: Int = 100) {
    var items: MutableList<T> = list
    val maxsize = size

    fun isEmpty():Boolean = this.items.isEmpty()

    fun count():Int = this.items.count()

    override fun toString() = this.items.toString()

    fun push(element: T){
        this.items.add(element)
        if (this.count() > this.maxsize) {
            this.items.removeAt(0)
        }
    }

    fun pop():T?{
        if (this.isEmpty()){
            return null
        } else {
            return this.items.removeAt(0)
        }
    }

    fun peek():T?{
        return this.items[0]
    }
}