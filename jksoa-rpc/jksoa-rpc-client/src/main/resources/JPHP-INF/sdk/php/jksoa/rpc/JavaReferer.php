<?php
namespace php\jksoa\rpc;

/**
 * Class JavaReferer
 * @packages php\jksoa\rpc
 */
class JavaReferer
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

}