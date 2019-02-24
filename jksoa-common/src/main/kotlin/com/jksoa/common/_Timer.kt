package com.jksoa.common

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

/**
 * 公共的毫秒级定时器
 *   HashedWheelTimer 是单线程的, 因此每个定时任务执行耗时不能太长, 如果有耗时任务, 则扔到其他线程池(如ForkJoinPool.commonPool())中处理
 */
public val CommonMilliTimer by lazy{
    HashedWheelTimer(1, TimeUnit.MILLISECONDS, 256 /* 2的次幂 */)
}

/**
 * 公共的秒级定时器
 *   HashedWheelTimer 是单线程的, 因此每个定时任务执行耗时不能太长, 如果有耗时任务, 则扔到其他线程池(如ForkJoinPool.commonPool())中处理
 */
public val CommonSecondTimer by lazy{
    HashedWheelTimer(200, TimeUnit.MILLISECONDS, 64 /* 2的次幂 */)
}

/**
 * 公共的线程池
 *   执行任务时要处理好异常
 */
public val CommonThreadPool: ForkJoinPool = ForkJoinPool.commonPool()