package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.ProductDetailModel
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 1/25/2018.
 */
interface ProductDetailsListener {
    fun onSuccessResponse(productData: ProductDetailModel?)
    fun onErrorResponse(error: VolleyErrorHelper?)
}