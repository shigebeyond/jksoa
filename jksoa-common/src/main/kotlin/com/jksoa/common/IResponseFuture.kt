package com.jksoa.common

import java.util.concurrent.Future

/**
 * 延后的响应
 *
 * @ClassName: ResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
interface IResponseFuture : Future<IResponse>, IResponse {

   /* fun onSuccess(response: IResponse)

    fun onFailure(response: IResponse)*/

}
