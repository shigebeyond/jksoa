package net.jkcode.jksoa.rpc.k8s

import java.io.File
import net.jkcode.jkutil.collection.FixedKeyMapFactory

/**
 * 模拟实现python pandas库的DataFrame
 */
class DataFrame(
    public val columns: List<String>, // 列
    protected val rows: List<List<String>> // 行
){

    /**
     * 数据的工厂
     */
    public val dataFactory: FixedKeyMapFactory by lazy {
        FixedKeyMapFactory(false, *columns.toTypedArray())
    }

    /**
     * @param colsAndRows 第一行是列名，其他行是行值
     */
    constructor(colsAndRows: List<List<String>>): this(colsAndRows[0], colsAndRows.subList(1, colsAndRows.size))

    /**
     * 获取指定单元格的值
     */
    fun getValue(row: Int, col: Int): String {
        return rows[row][col]
    }

    /**
     * 获取指定单元格的值
     */
    fun getValue(row: Int, col: String): String {
        return rows[row][columns.indexOf(col)]
    }

    /**
     * 获得指定列的所有值
     */
    fun getColumn(col: Int): List<String> {
        return rows.map { row ->
            row[col]
        }
    }

    /**
     * 获得指定列的所有值
     */
    fun getColumn(col: String): List<String> {
        return getColumn(columns.indexOf(col))
    }

    /**
     * 遍历行
     */
    fun forEach(action:(Map<String, String?>)->Unit){
        for (row in rows){
            val map = dataFactory.createMap(*row.toTypedArray())
            action(map)
        }
    }

    /**
     * 遍历行
     */
    fun <T> map(action:(Map<String, String?>)->T): List<T>{
        return rows.map {  row ->
            val map = dataFactory.createMap(*row.toTypedArray())
            action(map)
        }
    }

    companion object{

        // 空格的正则
        private val spaceReg = " +".toRegex()

        /**
         * 从字符串中解析DataFrame
         */
        public fun fromString(str: String): DataFrame?{
            val lines = str.split("\n")
            return fromLines(lines)
        }

        /**
         * 从csv中解析DataFrame
         */
        public fun fromCsv(file: String): DataFrame?{
            val lines = File(file).readLines()
            return fromLines(lines)
        }

        private fun fromLines(lines: List<String>): DataFrame? {
            if (lines.isEmpty())
                return null

            val colsAndRows = lines.map {
                it.split(spaceReg)
            }
            return DataFrame(colsAndRows)
        }




    }
}