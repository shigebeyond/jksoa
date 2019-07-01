package net.jkcode.jksoa.tracer.web.controller

import net.jkcode.jkmvc.http.controller.Controller

/**
 * 主页控制器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class IndexController: Controller()
{
    /**
     * 主页
     */
    public fun indexAction()
    {
        res.renderView("index")
    }

    /**
     * 查询页
     */
    public fun queryAction()
    {
        res.renderView("query")
    }
}
