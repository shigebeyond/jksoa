<?php
namespace net\jkcode\jksoa\rpc\example;

class ISimpleService {

	public static $_methodAnnotations = [];

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

	public static function ex(){
		return 'void ex()';
	}

}