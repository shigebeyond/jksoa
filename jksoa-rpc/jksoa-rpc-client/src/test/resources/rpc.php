<?php
use php\jksoa\rpc\Referer;

$ref = new Referer('net.jkcode.jksoa.rpc.example.ISimpleService');
$ret = $ref->ping();
echo "----------- php rpc call [ISimpleService.ping()], result: $ret\n";