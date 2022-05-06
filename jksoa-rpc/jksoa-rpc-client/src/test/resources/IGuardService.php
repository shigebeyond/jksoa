<?php
namespace net\jkcode\jksoa\rpc\example;

class IGuardService {

	public static $_methodAnnotations = ["getUserByNameAsync":["net.jkcode.jkguard.annotation.GroupCombine":["batchMethod":"\"listUsersByNameAsync\"","reqArgField":"\"name\"","respField":"\"\"","one2one":"true","flushQuota":"100","flushTimeoutMillis":"100"]],"listUsersByNameAsync":["net.jkcode.jkguard.annotation.RateLimit":["permitsPerSecond":"100.0","stablePeriodSeconds":"0","warmupPeriodSeconds":"0"]],"getUserWhenRandomException":["net.jkcode.jkguard.annotation.CircuitBreak":["type":"CircuitBreakType.EXCEPTION_COUNT","threshold":"1.0","checkBreakingSeconds":"5","breakedSeconds":"5","rateLimit":"RateLimit(0.0)"],"net.jkcode.jkguard.annotation.Metric":["bucketCount":"60","bucketMillis":"1000","slowRequestMillis":"10000"]],"getUserWhenException":["net.jkcode.jkguard.annotation.Degrade":["fallbackMethod":"\"getUserWhenFallback\""]],"getUserByIdAsync":["net.jkcode.jkguard.annotation.KeyCombine":[]]];

	public static function getUserByIdAsync($id){
		return 'java.util.concurrent.CompletableFuture getUserByIdAsync(int)';
	}

	public static function getUserByNameAsync($name){
		return 'java.util.concurrent.CompletableFuture getUserByNameAsync(java.lang.String)';
	}

	public static function listUsersByNameAsync($names){
		return 'java.util.concurrent.CompletableFuture listUsersByNameAsync(java.util.List)';
	}

	public static function getUserWhenException($id){
		return 'net.jkcode.jksoa.rpc.example.User getUserWhenException(int)';
	}

	public static function getUserWhenRandomException($id){
		return 'net.jkcode.jksoa.rpc.example.User getUserWhenRandomException(int)';
	}

}