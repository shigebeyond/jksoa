package com.jksoa.common

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

/**
 * 公共的定时器
 *   HashedWheelTimer 是单线程的, 因此每个定时任务执行耗时不能太长, 如果有耗时任务, 则扔到其他线程池(如ForkJoinPool.commonPool())中处理
 */
public val CommonTimer by lazy{
    HashedWheelTimer(1, TimeUnit.MILLISECONDS, 300)
}

/**
 * 公共的线程池
 *   执行任务时要处理好异常
 */
public val CommonThreadPool: ForkJoinPool = ForkJoinPool.commonPool()