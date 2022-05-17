<?php
namespace net\jkcode\jksoa\rpc\example;

class ISimpleService {

	// ------------ 方法注解 ------------
	public static $_methodAnnotations = [];

	// ------------ php对java调用映射的方法 ------------
	public static function hostname(){
		return 'java.lang.String hostname()';
	}

	public static function sayHi($msg){
		return 'java.lang.String sayHi(java.lang.String)';
	}

	public static function sleep(){
		return 'long sleep()';
	}

	public static function checkVersion(){
		return 'void checkVersion()';
	}

	public static function testException(){
		return 'void testException()';
	}

	// ------------ 降级方法 ------------
}