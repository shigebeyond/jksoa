package net.jkcode.jksoa.dtx.demo.order.service

import net.jkcode.jksoa.dtx.demo.product.ProductModel

/**
 * 商品服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 2:26 PM
 */
class ProductService {

    /**
     * 获得所有商品
     * @return
     */
    public fun getAllProducts(): List<ProductModel> {
        return ProductModel.queryBuilder().findAllModels<ProductModel>()
    }

    /**
     * 获得单个商品
     * @param id
     * @return
     */
    public fun getProductById(id: Int): ProductModel {
        return ProductModel(id).also {
            assert(it.loaded)
        }
    }

    /**
     * 获得多个商品
     * @param ids
     * @return
     */
    public fun getProductsByIds(ids: Collection<Int>): List<ProductModel> {
        return ProductModel.queryBuilder().where("id", "IN", ids).findAllModels<ProductModel>()
    }

}