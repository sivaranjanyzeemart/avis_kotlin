package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 12/12/2017.
 */
interface SupplierListResponseListener {
    fun onSuccessResponse(supplierList: List<DetailSupplierDataModel?>?)
    fun onErrorResponse(error: VolleyErrorHelper?)
}