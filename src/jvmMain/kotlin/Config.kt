object Config {
    var duplicateEnabled = false
    val dayOfWeekMap = mapOf(
        0 to "星期一",
        1 to "星期二",
        2 to "星期三",
        3 to "星期四",
        4 to "星期五",
        5 to "星期六",
        6 to "星期日"
    )

    // 默认是从周一到周五
    val dayOfWeekEnabledMap = mutableMapOf(
        0 to true,
        1 to true,
        2 to true,
        3 to true,
        4 to true,
        5 to false,
        6 to false
    )

    val selectedFoods = mutableMapOf(
        0 to "",
        1 to "",
        2 to "",
        3 to "",
        4 to "",
        5 to "",
        6 to ""
    )

    fun dayOfWeekEnabledList(): List<Int> {
        val result = ArrayList<Int>()
        for (day in dayOfWeekEnabledMap) {
            if (day.value) {
                result.add(day.key)
            }
        }
        return result
    }
}