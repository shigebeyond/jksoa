# Cluster

在 `net.jkcode.jksoa.job.cronjob.ClusterCronJobLauncher` 中实现了集群的调度者.

集群中有多个候选者节点, 但是只有选为leader的候选者节点才能成为唯一的调度者, 其他候选者节点则成为热备.

他的实现很简单, 就是使用`net.jkcode.jksoa.leader.ZkLeaderElection`来选举leader作为调度者.

```
package net.jkcode.jksoa.job.cronjob

import net.jkcode.jksoa.leader.ZkLeaderElection

/**
 * 集群实现的作业启动器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-29 11:48 AM
 */
abstract class ClusterCronJobLauncher {

    /**
     * 启动
     */
    public fun lauch(){
        // 选举领导者: 只有一个启动作业执行
        val election = ZkLeaderElection("cronJob")
        election.run(){
            // 加载cron与作业的复合表达式
            for(cronJobExpr in loadCronJobs()) {
                // 启动作业
                CronJobLauncher.lauch(cronJobExpr)
            }
        }
    }

    /**
     * 加载cron与作业的复合表达式
     * @return
     */
    public abstract fun loadCronJobs(): List<String>
}
```