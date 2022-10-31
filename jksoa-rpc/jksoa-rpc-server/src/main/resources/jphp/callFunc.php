<?php
define('APPPATH', dirname(__FILE__)); // 应用目录

// TODO: 为了优化性能, 可适当减少判断的代码, 如判断文件/类/方法存不存在, 以便减少php代码, 压榨点性能; 这样最后包一层try, 然后转化下对用户友好的异常

// 1 引入类文件
list($class, $method) = explode('::', $func);
// 加载类文件
$file = APPPATH . '/' . $class . '.php';
if(!file_exists($file)) // 检查class文件
    throw new \Exception("Class file not exists: $class");
include $file;

if(!class_exists($class, FALSE)) // 检查class类
    throw new \Exception("Class not exists: $class");

if (!method_exists($class, $method)) // 检查action方法
    throw new \Exception("Controller {$class} has no method: {$action}()");

// 2 调用方法
// return $class::$method($params); // 没有展开参数
return call_user_func_array([$class, $method], $params);
