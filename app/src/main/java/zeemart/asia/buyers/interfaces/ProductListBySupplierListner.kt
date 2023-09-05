package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.marketlist.ProductListBySupplier
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 12/12/2017.
 */
interface ProductListBySupplierListner {
    fun onSuccessResponse(productList: ProductListBySupplier?)
    fun onErrorResponse(error: VolleyErrorHelper?)
}