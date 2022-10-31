<?php
/**
 * Class P2pService
 *   用于辅助构建 P2pReferer.refererMethods
 */
class P2pService
{
    /**
     * 调用php文件
     * @param $file php文件名
     * @param $data 参数
     * @return
     */
    public static function callPhpFile($file, $data) {
        return "java.lang.Object callPhpFile(java.lang.String,java.util.Map)";
    }

    /**
     * 调用php方法
     * @param func php方法名, 如 User::sayHi()
     * @param params 方法参数
     * @return
     */
    public static function callPhpFunc($func, $args) {
        return "java.lang.Object callPhpFunc(java.lang.String,java.util.List)";
    }

}