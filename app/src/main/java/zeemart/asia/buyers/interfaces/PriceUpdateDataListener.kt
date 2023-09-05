package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse.PriceDetailModel
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 2/12/2018.
 */
interface PriceUpdateDataListener {
    fun onSuccessResponse(priceUpdateData: List<PriceDetailModel?>?)
    fun onErrorResponse(error: VolleyErrorHelper?)
}