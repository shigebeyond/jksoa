<?php
namespace php\jksoa\rpc;

/**
 * Class Referer
 * @packages php\jksoa\rpc
 */
class Referer
{
    /**
     * constructor.
     * @param string $obj
     * @throws IOException
     */
    function __construct($obj) {}

    function __call($method, $args) {}

    /**
     * Get name of class of object
     * @return string
     */
    public function getClassName() { }

    /**
     * 实例池: <接口类名, 实例>
     * @var array
     */
    protected static $_insts = [];

    /**
     * 根据接口类名获得实例
     * @param string $class
     * @return mixed
     */
    public static function instance($class)
    {
        if(!isset(static::$_insts[$class]))
            static::$_insts[$class] = new Referer($class); // 创建实例
        return static::$_insts[$class];
    }
}