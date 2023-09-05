package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.orderimportimport.CreateNewOrderResponseModel
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 12/17/2017.
 */
interface NewOrderResponseListener {
    fun onSuccessResponse(newOrderResponse: CreateNewOrderResponseModel?)
    fun onErrorResponse(error: VolleyErrorHelper?)
}