<?php
namespace net\jkcode\jksoa\rpc\example;

class ISimpleService {

	// ------------ 方法注解 ------------
	public static $_methodAnnotations = [];

	// ------------ php对java调用映射的方法 ------------
	public static function ping(){
		return 'java.lang.String ping()';
	}

	public static function echo($msg){
		return 'java.lang.String echo(java.lang.String)';
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