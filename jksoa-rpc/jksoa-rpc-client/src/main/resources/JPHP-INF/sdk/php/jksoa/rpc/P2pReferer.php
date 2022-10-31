<?php
namespace php\jksoa\rpc;

/**
 * Class PhpReferer
 * @packages php\jksoa\rpc
 */
class PhpReferer
{
    /**
     * constructor.
     * @param string javaClassName
     * @throws IOException
     */
    function __construct($javaClassName) {}

    /**
     * 调用php文件
     * @param $file php文件名
     * @param $data 参数
     * @return
     */
    public function callPhpFile($file, $data) { }

    /**
     * 调用php方法
     * @param func php方法名, 如 User::sayHi()
     * @param params 方法参数
     * @return
     */
    public function callPhpFunc($func, $args) { }

    /**
     * Get last call method
     * @return string
     */
    public function getLastCall() { }

}