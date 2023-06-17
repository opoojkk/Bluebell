package bean

import Config
import kotlin.math.absoluteValue
import kotlin.random.Random

class PickUpBean {
    private val original: ArrayList<Item> = ArrayList()
    private val result: HashMap<Int, String> = HashMap()
    fun add(item: Item) {
        original.add(item)
    }

    fun generate() {
        val originalCopy = ArrayList<Item>(original)
        val random = Random(System.currentTimeMillis())
        for (day in Config.dayOfWeekEnabledList()) {
            if (Config.selectedFoods[day] != "") {
                result[day] = Config.selectedFoods[day]!!
                continue
            }
            var index = (random.nextInt() % originalCopy.size).absoluteValue
            while (index == 0) {
                index = (random.nextInt() % originalCopy.size).absoluteValue
            }
            result[day] = originalCopy[index].name
            if (!Config.duplicateEnabled) {
                originalCopy.removeAt(index)
            }
        }
    }

    fun originalSize(): Int {
        return original.size
    }

    fun pickUpResult(): String {
        val stringBuilder = StringBuilder()
        for (day in Config.dayOfWeekEnabledList()) {
            stringBuilder.append(Config.dayOfWeekMap[day])
            stringBuilder.append(":")
            stringBuilder.append(result[day])
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

    fun pickUpPureResult(): String {
        return result.toString()
    }
}

class Item {
    var name: String = ""
    override fun toString(): String {
        return "Item(name='$name')"
    }

}