import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.read.listener.ReadListener
import java.awt.FileDialog
import java.io.File
import kotlin.random.Random


@Composable
@Preview
fun App() {
    var buttonText by remember { mutableStateOf("Hello, World!") }
    MaterialTheme {
        Button(onClick = {
            val fileDialog = FileDialog(ComposeWindow())
            fileDialog.isVisible = true
            val path = "${fileDialog.directory}${fileDialog.file}"
            println("path: $path")
            val file = File(path)
            if (!file.exists() || !file.isFile) {
                println("选错啦！")
                return@Button
            }
            if (!file.path.contains(".xlsx")) {
                println("xlsx格式")
                return@Button
            }
            val seed = 1
            val random = Random(seed)
            val original = ArrayList<Item>()
            val result = ArrayList<Item>()
            EasyExcel.read(file.path, Item::class.java, object : ReadListener<Item> {
                override fun invoke(data: Item?, context: AnalysisContext?) {
                    println("$data")
                    data ?: return
                    original += data
                }

                override fun doAfterAllAnalysed(context: AnalysisContext?) {
                    println("done")
                    for (day in 0..4) {
                        val index = random.nextInt() % original.size
                        result += original[index]
                    }
                    println("$result")
                }
            }).doReadAll();
        }) {

        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

class Item() {
    var name: String = ""
    override fun toString(): String {
        return "Item(name='$name')"
    }

}