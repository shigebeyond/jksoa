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