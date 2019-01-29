package com.jksoa.leader

import com.jkmvc.closing.ClosingOnShutdown

/**
 * 选举领导者接口
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
interface ILeaderElection {

    /**
     * 团队名
     */
    val teamName: String

    /**
     * 我是否是leader
     */
    public val isLeader: Boolean

    /**
     * 我的数据, 每个候选人节点都必须持有不同的数据
     */
    val myData: String

    /**
     * 领导者的数据
     */
    val leaderData: String

    /**
     * 成功回调
     */
    val callback: (ZkLeaderElection)->Unit

    /**
     * 开始选举
     */
    fun start()
}