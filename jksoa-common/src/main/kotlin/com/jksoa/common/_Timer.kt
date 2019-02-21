package com.jksoa.common

import io.netty.util.HashedWheelTimer
import java.util.concurrent.TimeUnit

/**
 * 共同定时器
 */
public val CommonTimer by lazy{
    HashedWheelTimer(10, TimeUnit.MILLISECONDS, 300)
}