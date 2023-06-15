import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import bean.Item
import bean.PickUpBean
import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.read.listener.ReadListener
import exception.IllegalFileFormatException
import java.awt.FileDialog
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    var randomResult by remember { mutableStateOf("") }
    var duplicateAllowed by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    var state by remember { mutableStateOf(StateConstant.Normal) }

    MaterialTheme {
        Column(modifier = Modifier.wrapContentHeight().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextField(
                    readOnly = true,
                    modifier = Modifier.weight(1F).wrapContentHeight(),
                    placeholder = { Text("选择文件后显示路径") },
                    value = selectedFile?.path ?: "",
                    onValueChange = { },
                    shape = RoundedCornerShape(10f),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        val file = try {
                            selectFile()
                        } catch (e: NoSuchFileException) {
                            state = StateConstant.NoFileSelected
                            return@Button
                        } catch (e: IllegalArgumentException) {
                            state = StateConstant.CantFindFile
                            return@Button
                        } catch (e: IllegalFileFormatException) {
                            state = StateConstant.WrongFileFormat
                            return@Button
                        }
                        selectedFile = file
                    }
                ) {
                    Text("选择文件")
                }
                alertAlertDialog(
                    state = state,
                    onDismiss = { state = StateConstant.Normal })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    modifier = Modifier.padding(start = 8.dp),
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
            if (randomResult.isNotEmpty()) {
                Text(text = randomResult, softWrap = true)
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                selectedFile ?: let {
                    state = StateConstant.NoFileSelected
                    return@Button
                }
                try {
                    readFileParseResult(selectedFile!!, callback = {
                        if (it.originalSize() < 5 && !duplicateAllowed) {
                            throw IllegalArgumentException()
                        }
                        randomResult = it.processPickUpResult()
                    })
                } catch (illegalArgumentException: IllegalArgumentException) {
                    state = StateConstant.NotEnoughOptions
                }

            }) {
                Text("生成")
            }
            alertAlertDialog(
                state = state,
                onDismiss = { state = StateConstant.Normal })
        }
    }
}

private fun readFileParseResult(file: File, callback: (PickUpBean) -> Unit) {
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
            callback(pickUpBean)
        }
    }).doReadAll()
}

private fun selectFile(): File {
    val fileDialog = FileDialog(ComposeWindow())
    fileDialog.isVisible = true
    val path = "${fileDialog.directory}${fileDialog.file}"
    val file = File(path)
    if (!file.exists()) {
        throw NoSuchFileException(file)
    }
    if (!file.isFile) {
        throw IllegalArgumentException("not a file")
    }
    if (!file.path.contains(".xlsx")) {
        throw IllegalFileFormatException("error file format")
    }
    return file
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun alertAlertDialog(
    state: StateConstant,
    onDismiss: () -> Unit
) {
    if (state == StateConstant.Normal) {
        return
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier
            .padding(28.dp)
            .wrapContentWidth()
            .wrapContentHeight(),
        title = null,
        text = {
            Text(text = state.content)
        },

        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "OK")
            }
        }
    )
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
