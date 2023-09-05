package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by ParulBhandari on 11/10/2017.
 * Return response for the reset Password API
 */
interface GetRequestStatusResponseListener {
    fun onSuccessResponse(status: String?)
    fun onErrorResponse(error: VolleyErrorHelper?)
}