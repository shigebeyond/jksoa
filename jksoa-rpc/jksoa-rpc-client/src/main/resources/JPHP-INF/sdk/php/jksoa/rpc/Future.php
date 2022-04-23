<?php
namespace php\jksoa\rpc;

/**
 * Class Future
 * @package php\jksoa\rpc
 */
class Future {


    private function __construct() {}

    /**
     * @return bool
     */
    public function isCancelled() { }

    /**
     * @return bool
     */
    public function isDone() { }

    /**
     * @param bool $mayInterruptIfRunning
     * @return bool
     */
    public function cancel($mayInterruptIfRunning) { }

    /**
     * @param null|int $timeout - in milliseconds
     * @return mixed
     * @throws \Exception
     */
    public function get($timeout = null) { }
}
