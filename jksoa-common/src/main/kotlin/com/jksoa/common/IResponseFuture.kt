package com.jksoa.common

import java.util.concurrent.Future


interface IResponseFuture : Future<IResponse>, IResponse {

    fun onSuccess(response: IResponse)

    fun onFailure(response: IResponse)

}
