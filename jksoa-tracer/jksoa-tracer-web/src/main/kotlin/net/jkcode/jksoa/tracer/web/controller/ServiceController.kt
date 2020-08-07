package net.jkcode.jksoa.tracer.web.controller

import net.jkcode.jkmvc.http.controller.Controller
import net.jkcode.jksoa.tracer.common.model.service.AppModel
import net.jkcode.jksoa.tracer.common.model.service.ServiceModel

/**
 * 服务信息查询的控制器
 * 
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class ServiceController: Controller()
{
    /**
     * 查询应用列表
     */
    public fun appListAction()
    {
        val items = AppModel.queryBuilder().findModels<AppModel>()
        res.renderJson(items)
    }

    /**
     * 查询某应用下的服务列表
     */
    public fun listAction(){
        val appId: Int = req["id"]!!
        val items = ServiceModel.queryBuilder().where("app_id", appId).findModels<ServiceModel>()
        res.renderJson(items)
    }

}
