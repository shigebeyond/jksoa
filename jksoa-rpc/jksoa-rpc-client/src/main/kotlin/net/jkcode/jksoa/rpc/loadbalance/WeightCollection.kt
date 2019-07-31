package net.jkcode.jksoa.rpc.loadbalance

import net.jkcode.jkmvc.common.joinToString
import net.jkcode.jksoa.rpc.client.IConnection
import java.util.NoSuchElementException

/**
 * 有权重的集合
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-31 7:39 PM
 */
class WeightCollection(protected val conns: Collection<IConnection>) : Collection<IConnection> by conns {

    /**
     * 集合大小 = 权重综合
     */
    public override val size: Int
        get() = conns.sumBy { it.weight }

    /**
     * 有权重的迭代器
     */
    public override fun iterator(): Iterator<IConnection> {
        return WeightIterator()
    }

    override fun toString(): String {
        return iterator().joinToString(", ", "WeightCollection[", "]")
    }

    /**
     * 有权重的迭代器
     */
    inner class WeightIterator: Iterator<IConnection>{

        /**
         * 连接迭代器
         */
        protected val itr: Iterator<IConnection> = conns.iterator()

        /**
         * 当前连接
         */
        protected var _conn: IConnection? = prepareConn()

        /**
         * 下一序号
         */
        protected var _next = prepareNext(-1)

        /**
         * 准备下一连接
         * @return
         */
        protected fun prepareConn(): IConnection? {
            return  if (itr.hasNext()) itr.next() else null
        }

        /**
         * 准备下一序号
         * @param start 开始序号
         * @return
         */
        protected fun prepareNext(start: Int): Int {
            var i = start
            // 有连接
            while(_conn != null) {
                // 下一个序号
                if (++i < _conn!!.weight)
                    return i

                // 换下一个连接
                _conn = prepareConn()
                // 换下序号
                i = -1
            }
            return -1
        }

        public override fun hasNext(): Boolean {
            return _next != -1;
        }

        public override fun next(): IConnection {
            if (_next == -1)
                throw NoSuchElementException();

            val currConn = _conn!!
            _next = prepareNext(_next) // 准备下一序号
            return currConn
        }

    }

}