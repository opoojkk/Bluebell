enum class StateConstant {
    Normal,
    NotEnoughOptions("小笨蛋，选项不够还不让重复，\n怎么生成呢?"),
    NoFileSelected("小笨蛋，先选一个文件"),
    CantFindFile("小笨蛋，刚选的刚选的可不是一个文件"),
    WrongFileFormat("小笨蛋，得是Excel的格式");


    val content: String

    constructor() : this("")
    constructor(content: String) {
        this.content = content
    }
}