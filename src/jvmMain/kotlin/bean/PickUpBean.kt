package bean

import kotlin.math.absoluteValue
import kotlin.random.Random

class PickUpBean {
    private val original: ArrayList<Item> = ArrayList()
    private val result: ArrayList<Item> = ArrayList()

    fun add(item: Item) {
        original.add(item)
    }

    fun create() {
        val random = Random(System.currentTimeMillis())
        for (day in 0..4) {
            val index = (random.nextInt() % original.size).absoluteValue
            result += original[index]
        }
    }

    fun originalSize(): Int {
        return original.size
    }

    fun processPickUpResult(): String {
        val stringBuilder = StringBuilder()
        for (day in 0..4) {
            stringBuilder.append(dayOfTheWeek(day))
            stringBuilder.append(":")
            val item = result[day]
            stringBuilder.append(item.name)
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    private fun dayOfTheWeek(day: Int): String {
        return when (day) {
            0 -> "星期一"
            1 -> "星期二"
            2 -> "星期三"
            3 -> "星期四"
            4 -> "星期五"
            5 -> "星期六"
            6 -> "星期日"
            else -> ""
        }
    }
}

class Item() {
    var name: String = ""
    override fun toString(): String {
        return "Item(name='$name')"
    }

}