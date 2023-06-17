enum class StateConstant {
    Normal,
    NotEnoughOptions("选项不够还不让重复，\n怎么生成呢?"),
    FilePathError("文件路径有问题还是没有选文件"),
    CantFindFile("小笨蛋，刚选的刚选的可不是一个文件"),
    DeselectFile("没有选择文件呀"),
    WrongFileFormat("小笨蛋，Excel格式的文件");


    val content: String

    constructor() : this("")
    constructor(content: String) {
        this.content = content
    }
}