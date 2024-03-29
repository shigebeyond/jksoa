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
     * @param string phpClassName
     * @throws IOException
     */
    function __construct($phpClassName) {}

    function __call($method, $args) {}

    /**
     * Get last call method
     * @return string
     */
    public function getLastCall() { }

    /**
     * Get name of class of object
     * @return string
     */
    public function getClassName() { }

}