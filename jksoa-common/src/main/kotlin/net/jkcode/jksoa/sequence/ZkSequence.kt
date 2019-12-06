package net.jkcode.jksoa.sequence

import net.jkcode.jkutil.scope.ClosingOnShutdown
import net.jkcode.jkutil.common.JkApp
import net.jkcode.jkutil.common.getOrPutOnce
import net.jkcode.jksoa.zk.ZkClientFactory
import org.I0Itec.zkclient.IZkChildListener
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkNodeExistsException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 序列号生成器: 基于zk的持久顺序节点来实现
 * 　　为成员生成唯一的序列号
 *
 * zk目录结构如下:
 * ```
 * sequence
 * 	module1 # 模块的根节点
 * 		_first-0 # 第一个节点, 仅用于占住0序号
 * 		mem1-0000000001 # 其他节点: 成员名-序号
 * 		mem2-0000000002
 * 		mem3-0000000003
 * ```
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-11 12:24 PM
 */
class ZkSequence protected constructor(public override val module: String /* 模块 */) : ClosingOnShutdown(), ISequence {

    companion object {
        /**
         * zk根节点路径
         */
        public val RootPath: String = "/sequence"

        /**
         * 成员路径的分隔符
         */
        public val MemberPathDelimiter = '-'

        /**
         * zk客户端
         */
        public val zkClient: ZkClient = ZkClientFactory.instance()

        /**
         * 单例池
         */
        protected var insts: ConcurrentHashMap<String, ZkSequence> = ConcurrentHashMap();

        /**
         * 获得单例
         */
        public fun instance(group: String): ZkSequence {
            return insts.getOrPutOnce(group) {
                ZkSequence(group)
            }
        }
    }

    /**
     * zk的父节点路径
     */
    protected val parentPath: String = "$RootPath/$module"

    /**
     * 成员数据
     */
    protected val memberData: String = JkApp.fullWorkerId

    /**
     * 成员映射序号
     *    member-> 00001
     */
    protected var mem2no: ConcurrentHashMap<String, Int> = ConcurrentHashMap();

    /**
     * 序号映射成员
     *    00001 -> member
     */
    protected var no2mem: ConcurrentHashMap<Int, String> = ConcurrentHashMap();

    /**
     * 子节点变化监听器
     */
    protected var childListener: IZkChildListener? = null

    init {
        // 1 创建根节点
        if (!zkClient.exists(parentPath))
            try{
                // 第一个节点是_first-0, 仅用于占住0序号
                zkClient.createPersistent(parentPath + "/_first-0", true)
            } catch (e: ZkNodeExistsException) {
                // do nothing
            }

        // 2 先查结果
        val childrenKeys = zkClient.getChildren(parentPath)
        updateSequences(childrenKeys)

        // 3 监听子节点变化
        childListener = object: IZkChildListener{
            // 处理zk中子节点变化事件
            override fun handleChildChange(parentPath: String, childPaths: List<String>) {
                // 更新成员序号
                updateSequences(childPaths)
            }
        }
        zkClient.subscribeChildChanges(parentPath, childListener)
    }

    /**
     * 根据子节点路径, 更新成员序号
     * @param childPaths 子节点路径
     */
    protected fun updateSequences(childPaths: List<String>) {
        for (path in childPaths)
            updateSequence(path)
    }

    /**
     * 根据子节点路径, 更新成员序号
     * @param childPath 子节点路径
     */
    protected fun updateSequence(childPath: String) {
        if (childPath.contains(MemberPathDelimiter)) {
            val (member, noStr) = childPath.split(MemberPathDelimiter)
            val no = noStr.toInt()
            mem2no.put(member, no) // 成员->序号
            no2mem.put(no, member) // 序号->成员
        }
    }

    /**
     * 获得成员序号, 没有则创建
     * @param member
     * @return
     */
    public override fun getOrCreate(member: String): Int {
        // 没有序号, 则通过创建顺序节点来获得序号
        if(!mem2no.containsKey(member)) {
            // 创建顺序节点
            val path = zkClient.createPersistentSequential(parentPath + '/' + member + MemberPathDelimiter, memberData)
            val childPath = path.substring(parentPath.length + 1)
            updateSequence(childPath)
        }

        return mem2no[member]!!
    }

    /**
     * 获得成员序号, 没有则抛异常
     * @param member
     * @return
     */
    public override fun get(member: String): Int{
        return mem2no[member] ?: throw NoSuchElementException("模块[$module]中没有成员[$member]对应的序号")
    }

    /**
     * 根据序号获得成员, 没有则抛异常
     * @param no
     * @return
     */
    public override fun get(no: Int): String{
        return no2mem[no] ?: throw NoSuchElementException("模块[$module]中没有序号[$no]对应的成员")
    }

    /**
     * 关闭选举
     */
    public override fun close() {
        // 取消订阅
        if(childListener != null)
            zkClient.unsubscribeChildChanges(parentPath, childListener)
    }

}
