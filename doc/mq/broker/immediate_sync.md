# 立即同步

jksoa-mq支持缓冲消息文件的写操作, 然后批量同步到磁盘, 即异步定时定量刷盘.

其中, 配置文件 `broker.yaml`的属性`batchSyncQuota` 与 `batchSyncTimeoutMillis` 控制对消息的写操作是否批量同步到磁盘:

```
# 批量同步的配置
# 只有 batchSyncQuota 与 batchSyncTimeoutMillis 都大于0才批量同步, 否则立即同步
batchSyncQuota: 100 # 触发批量同步的写操作次数
batchSyncTimeoutMillis: !!java.lang.Long 1000 # 触发批量同步的定时时间
```

如果你实现立即同步, 则需要将属性`batchSyncQuota` 与 `batchSyncTimeoutMillis` 都设为0

但注意的是, 如果broker在定时刷盘的时间间隔内停机了, 则会导致该时期内的缓冲的写操作丢失.