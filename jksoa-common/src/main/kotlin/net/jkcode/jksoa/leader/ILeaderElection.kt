package net.jkcode.jksoa.leader

/**
 * 选举领导者接口
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
interface ILeaderElection {

    /**
     * 组名
     */
    val group: String

    /**
     * 成员数据, 每个候选人节点都尽量持有不同的数据, 如服务地址
     */
    val memberData: String

    /**
     * 监听选举结果 / listen to the election result
     * @param callback 选举结果的回调
     */
    fun listen(callback: (String)->Unit)

    /**
     * 参选 / run for election
     * @param callback 成功回调
     */
    fun run(callback: ()->Unit)

}