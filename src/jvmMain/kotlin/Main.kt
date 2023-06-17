import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
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


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    var randomResult by remember { mutableStateOf("") }
    var pureRandomResult by remember { mutableStateOf("") }
    var duplicateAllowed by remember { mutableStateOf(Config.duplicateEnabled) }
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
                            state = StateConstant.FilePathError
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
                    Text("选择文件⓵")
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
                        Config.duplicateEnabled = it
                    }, colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colors.primary,
                        uncheckedColor = MaterialTheme.colors.primary
                    )
                )
                Text("允许重复（默认不允许）", modifier = Modifier.onClick {
                    duplicateAllowed = !duplicateAllowed
                })
            }
            var loading by remember { mutableStateOf(false) }
            var pickUpBean by remember { mutableStateOf(PickUpBean()) }
            Row {
                ReadFileButton(
                    modifier = Modifier.weight(1f),
                    selectedFile = selectedFile,
                    stateChange = { state = it },
                    onStart = { loading = true },
                    onFinish = {
                        pickUpBean = it
                        loading = false
                    })
                Spacer(modifier = Modifier.width(8.dp))
                PreferencesButton(modifier = Modifier.weight(1f))
            }
            Button(onClick = {
                pickUpBean.generate()
                randomResult = pickUpBean.pickUpResult()
                pureRandomResult = pickUpBean.pickUpPureResult()
            }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("生成⓸")
            }
            Loading(loading)
            if (randomResult.isNotEmpty()) {
                var copyText by remember { mutableStateOf("") }
                Text(text = randomResult, softWrap = true, modifier = Modifier.padding(top = 8.dp))
                Row {
                    Button(modifier = Modifier.weight(1f), onClick = {
                        copyText = randomResult
                    }) {
                        Text("复制全部")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(modifier = Modifier.weight(1f), onClick = {
                        copyText = pureRandomResult
                    }) {
                        Text("只复制内容（不包含周几）")
                    }
                    if (copyText.isNotEmpty()) {
                        copyToClipBroad(copyText)
                        copyText = ""
                    }
                }
            }
            alertAlertDialog(
                state = state,
                onDismiss = { state = StateConstant.Normal })
        }
    }
}

@Composable
private fun copyToClipBroad(randomResult: String) {
    LocalClipboardManager.current.setText(AnnotatedString(randomResult))
}

private fun readFileParseResult(
    file: File,
    onStart: () -> Unit,
    onReaded: (PickUpBean) -> Unit,
    onFinish: (PickUpBean) -> Unit
) {
    val pickUpBean = PickUpBean()
    onStart()
    EasyExcel.read(file.path, Item::class.java, object : ReadListener<Item> {
        override fun invoke(data: Item?, context: AnalysisContext?) {
            println("$data")
            data ?: return
            pickUpBean.add(data)
        }

        override fun doAfterAllAnalysed(context: AnalysisContext?) {
            onReaded(pickUpBean)
            onFinish(pickUpBean)
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

@Composable
fun Loading(animate: Boolean) {
    if (!animate) {
        return
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .height(60.dp)
                .width(60.dp)
                .padding(top = 8.dp),
            color = MaterialTheme.colors.primary,
            strokeWidth = 5.dp
        )
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun DayOfWeekLazyColumn() {
    LazyColumn {
        items(Config.dayOfWeekMap.size) { index ->
            CustomPreferences(index)
        }
    }
}

@Composable
private fun CustomPreferences(index: Int) {
    val enabled = remember { mutableStateOf(Config.dayOfWeekEnabledMap[index]!!) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colors.primary,
                uncheckedColor = MaterialTheme.colors.primary
            ),
            checked = enabled.value,
            onCheckedChange = {
                enabled.value = it
                Config.dayOfWeekEnabledMap[index] = it
            })
        Text(text = Config.dayOfWeekMap[index]!!)
        val food = remember { mutableStateOf(Config.selectedFoods[index]) }
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            enabled = enabled.value,
            value = food.value!!,
            onValueChange = {
                food.value = it
                Config.selectedFoods[index] = it
            },
            modifier = Modifier.scale(scaleY = 0.8F, scaleX = 0.8F).weight(1f),
            singleLine = true,
            placeholder = {
                Text(
                    text = "固定餐品名称（不再随机）",
                    color = if (enabled.value) Color(0xFF666666) else Color(0xFF999999)
                )
            }
        )
    }
}

fun main() = application {
    Window(
        state = WindowState(width = 530.dp, height = 440.dp, position = WindowPosition.Aligned(Alignment.Center)),
        title = "随机餐品",
        onCloseRequest = ::exitApplication,
        icon = BitmapPainter(useResource("heibao.png", ::loadImageBitmap))
    ) {
        App()
    }
}

@Preview
@Composable
private fun PreferencesButton(modifier: Modifier = Modifier) {
    val show = remember { mutableStateOf(false) }
    Button(modifier = modifier, onClick = {
        show.value = true
    }) {
        Text("配置项（可选）⓷", textAlign = TextAlign.Center)
    }
    if (show.value) {

        Window(
            state = WindowState(
                width = 420.dp,
                height = 440.dp,
                position = WindowPosition.Aligned(Alignment.Center)
            ),
            title = "配置项",
            onCloseRequest = { show.value = false },
            resizable = false,
            icon = BitmapPainter(useResource("heibao.png", ::loadImageBitmap))
        ) {
            DayOfWeekLazyColumn()
        }
    }
}

@Preview
@Composable
private fun ReadFileButton(
    modifier: Modifier = Modifier,
    selectedFile: File?,
    duplicateAllowed: Boolean = false,
    stateChange: (StateConstant) -> Unit,
    onStart: () -> Unit,
    onFinish: (PickUpBean) -> Unit
) {
    Button(modifier = modifier, onClick = {
        if (selectedFile == null) {
            stateChange(StateConstant.DeselectFile)
            return@Button
        }
        Thread {
            try {
                readFileParseResult(
                    selectedFile,
                    onStart = onStart,
                    onReaded = {
                        if (it.originalSize() < 5 && !duplicateAllowed) {
                            throw IllegalArgumentException()
                        }
                    },
                    onFinish = onFinish
                )
            } catch (illegalArgumentException: IllegalArgumentException) {
                stateChange(StateConstant.NotEnoughOptions)
            }
        }.start()
    }) {
        Text("解析⓶", textAlign = TextAlign.Center)
    }
}
