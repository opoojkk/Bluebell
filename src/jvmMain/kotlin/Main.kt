import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.read.listener.ReadListener
import kotlinx.coroutines.DelicateCoroutinesApi
import java.awt.Dialog
import java.awt.FileDialog
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random


@OptIn(DelicateCoroutinesApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    var pathFile by remember { mutableStateOf("") }
    var randomResult by remember { mutableStateOf("") }
    var duplicateAllowed by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    var selectFileDialogEnabled by remember { mutableStateOf(false) }
    MaterialTheme {
        Column(modifier = Modifier.wrapContentHeight().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextField(
                    readOnly = true,
                    modifier = Modifier.weight(1F).wrapContentHeight(),
                    placeholder = { Text("input file path") },
                    value = selectedFile?.path ?: "",
                    onValueChange = { },
                    shape = RoundedCornerShape(10f),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
//                Text(
//                    text = selectedFile?.name ?: pathFile,
//                    modifier = Modifier
//                        .weight(1F)
//                        .wrapContentHeight()
//                        .padding(start = 8.dp)
//                        .background(Color.LightGray, RoundedCornerShape(10f))
//                )
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        val file = try {
                            selectFile()
                        } catch (e: Exception) {
                            // TODO: 弹窗
                            return@Button
                        }
                        selectedFile = file
                    }) {
                    Text("select file")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = duplicateAllowed,
                    onCheckedChange = {
                        duplicateAllowed = it
                    }, colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colors.primary,
                        uncheckedColor = MaterialTheme.colors.primary
                    )
                )
                Text("允许重复（默认不允许）", modifier = Modifier.onClick {
                    duplicateAllowed = !duplicateAllowed
                })
            }
            Text(text = randomResult, softWrap = true)
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                selectedFile ?: let {
                    selectFileDialogEnabled = true
                    return@Button
                }
                selectedFile?.let { file ->
                    readFileParseResult(file, callback = {
                        randomResult = it
                    })
                }
            }) {
                Text("生成")
            }
            selectFileDialog(selectFileDialogEnabled, dismissCallback = {
                selectFileDialogEnabled = false
            })
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun selectFileDialog(selectFileDialogEnabled: Boolean, dismissCallback: () -> Unit) {
    if (!selectFileDialogEnabled) {
        return
    }
    AlertDialog(
        onDismissRequest = { dismissCallback() },
        modifier = Modifier
            .padding(28.dp)
            .wrapContentWidth()
            .wrapContentHeight(),
        title = null,
        text = {
            Text("请先选择文件")
        },
        confirmButton = {
            TextButton(onClick = { dismissCallback() }) {
                Text(text = "OK")
            }
        })
}

private fun readFileParseResult(file: File, callback: (String) -> Unit) {
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
            callback(pickUpBean.processPickUpResult())
        }
    }).doReadAll()
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
        state = WindowState(width = 400.dp, position = WindowPosition.Aligned(Alignment.Center)),
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