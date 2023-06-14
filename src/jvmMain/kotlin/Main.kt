import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.read.listener.ReadListener
import kotlinx.coroutines.DelicateCoroutinesApi
import java.awt.FileDialog
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random


@OptIn(DelicateCoroutinesApi::class)
@Composable
@Preview
fun App() {
    var pathFile by remember { mutableStateOf("") }
    var randomResult by remember { mutableStateOf("") }
    MaterialTheme {
        Column {
            Row {
                TextField(
                    label = { Text("input file path") },
                    value = pathFile,
                    onValueChange = {
                        pathFile = it
                    })
                Button(onClick = {
                    val file = try {
                        selectFile()
                    } catch (e: Exception) {
                        // TODO: 弹窗
                        return@Button
                    }


                    val pickUpBean = PickUpBean()
                    EasyExcel.read(file.path, Item::class.java, object : ReadListener<Item> {
                        override fun invoke(data: Item?, context: AnalysisContext?) {
                            println("$data")
                            data ?: return
                            pickUpBean.add(data)
                        }

                        override fun doAfterAllAnalysed(context: AnalysisContext?) {
                            println("done")
                            pickUpBean.create()
                            randomResult = pickUpBean.generate()
                            println(randomResult)
                        }
                    }).doReadAll();
                }) {
                    Text("select file")
                }
            }
            Text(randomResult, softWrap = true)
        }
    }
}

private fun selectFile(): File {
    val fileDialog = FileDialog(ComposeWindow())
    fileDialog.isVisible = true
    val path = "${fileDialog.directory}${fileDialog.file}"
    println("path: $path")
    val file = File(path)
    if (!file.exists() || !file.isFile) {
        throw Exception("文件不存在")
    }
    if (!file.path.contains(".xlsx")) {
        throw Exception("文件格式错误")
    }
    return file
}

fun main() = application {
    Window(
        state = WindowState(width = 400.dp, height = 200.dp, position = WindowPosition.Aligned(Alignment.Center)),
        title = "随机抽取",
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}

class Item() {
    var name: String = ""
    override fun toString(): String {
        return "Item(name='$name')"
    }

}

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

    fun generate(): String {
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
            else -> "星期六"
        }
    }
}