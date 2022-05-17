<?php
use php\jksoa\rpc\JavaReferer;
use php\jksoa\rpc\PhpReferer;

function print_rpc($ref, $ret){
   if(is_array($ret))
      $ret = print_r($ret, true);
   echo "----------- php rpc call [{$ref->getLastCall()}], result: $ret\n";
}

/*
// java引用对象
// $ref = new JavaReferer('net.jkcode.jksoa.rpc.example.ISimpleService');
// php引用对象
include 'src/test/resources/ISimpleService.php';
$ref = new PhpReferer('net\jkcode\jksoa\rpc\example\ISimpleService');
// rpc
$ret = $ref->hostname();
print_rpc($ref, $ret);
$ret = $ref->sayHi('hello');
print_rpc($ref, $ret);
*/

// php引用对象
include 'src/test/resources/IPhpGuardService.php';
$ref = new PhpReferer('net\jkcode\jksoa\rpc\example\IPhpGuardService');
// rpc
$ret = $ref->getUserByIdAsync(1)->get();
print_rpc($ref, $ret);