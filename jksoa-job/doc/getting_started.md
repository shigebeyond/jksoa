方法中不要使用默认参数, 否则以下的 RpcRquest 构造函数无法识别, 但是手动构造 RpcRquest 只在 job 中使用
public constructor(func: KFunction<*>, args: Array<Any?> = emptyArray()) : this(func.javaMethod!!, args)