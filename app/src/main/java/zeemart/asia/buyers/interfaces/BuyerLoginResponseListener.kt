package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 11/10/2017.
 * Interface that provide a callback when buyer Login API is called
 */
interface BuyerLoginResponseListener {
    fun onSuccessResponse(buyerDetails: BuyerDetails?)
    fun onErrorResponse(error: VolleyErrorHelper?)
}