package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.RetrieveSpecificPromoCode
import zeemart.asia.buyers.models.ValidatePromoCode
import zeemart.asia.buyers.modelsimport.RetrieveMultiplePromoCodes

object PromoCodeApi {
    // api id - 22.1 Validate promotion code
    @JvmStatic
    fun validatePromotionCode(
        context: Context?,
        request: ValidatePromoCode?,
        outletId: String?,
        listener: ValidatePromoCodeListener
    ) {
        val Uri = ServiceConstant.ENDPOINT_VALIDATE_PROMOTION_CODE
        val requestJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(request)
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(outlet)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            Uri,
            requestJson,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val validatePromoCodeResponseResponse =
                    fromJson(response, ValidatePromoCode.Response::class.java)
                listener.onSuccessResponse(validatePromoCodeResponseResponse)
            }) { error -> listener.onErrorResponse(error) }
        VolleyRequest.getInstance(context)?.addToRequestQueue(zeemartAPIRequest)
    }

    // api id - 22.3 Retrieve Multiple promotion codes
    @JvmStatic
    fun retrieveMultiplePromotionCodes(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        outlets: List<Outlet>?,
        listener: MultiplePromoCodesResponseListener
    ) {
        var url = ServiceConstant.ENDPOINT_PROMOTION_CODES
        url = apiParamsHelper.getUrl(url)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            url,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val multiplePromoCodesData =
                    fromJson(response, RetrieveMultiplePromoCodes.Response::class.java)
                listener.onSuccessResponse(multiplePromoCodesData)
            }) { error -> listener.onErrorResponse(error) }
        VolleyRequest.getInstance(context)?.addToRequestQueue(zeemartAPIRequest)
    }

    //  api id - 22.2 Retrieve Specific promotion code
    @JvmStatic
    fun retrieveSpecificPromoCode(
        context: Context?,
        promoCodeId: String?,
        outlets: List<Outlet>?,
        listener: RetrieveSpecificPromoCodeResponseListener
    ) {
        var url = ServiceConstant.ENDPOINT_PROMOTION_CODE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setId(promoCodeId!!)
        url = apiParamsHelper.getUrl(url)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            url,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val retrieveSpecificPromoCode =
                    fromJson(response, RetrieveSpecificPromoCode::class.java)
                listener.onSuccessResponse(retrieveSpecificPromoCode)
            }) { error -> listener.onErrorResponse(error) }
        VolleyRequest.getInstance(context)?.addToRequestQueue(zeemartAPIRequest)
    }

    interface ValidatePromoCodeListener {
        fun onSuccessResponse(response: ValidatePromoCode.Response?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface MultiplePromoCodesResponseListener {
        fun onSuccessResponse(response: RetrieveMultiplePromoCodes.Response?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface RetrieveSpecificPromoCodeResponseListener {
        fun onSuccessResponse(response: RetrieveSpecificPromoCode?)
        fun onErrorResponse(error: VolleyError?)
    }
}