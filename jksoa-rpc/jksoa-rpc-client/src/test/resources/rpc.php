<?php
use php\jksoa\rpc\JavaReferer;
use php\jksoa\rpc\PhpReferer;

// java引用对象
// $ref = new JavaReferer('net.jkcode.jksoa.rpc.example.ISimpleService');
// php引用对象
include 'src/test/resources/ISimpleService.php';
$ref = new PhpReferer('net\jkcode\jksoa\rpc\example\ISimpleService');
// rpc
// $ret = $ref->ping();
// echo "----------- php rpc call [ISimpleService.ping()], result: $ret\n";
$ret = $ref->echo('hello');
echo "----------- php rpc call [ISimpleService.echo()], result: $ret\n";

