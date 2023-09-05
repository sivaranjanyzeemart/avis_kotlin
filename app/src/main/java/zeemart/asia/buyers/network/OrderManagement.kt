package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.orderimport.CreateDraftOrder
import zeemart.asia.buyers.models.orderimport.NewCreateOrderResponse
import zeemart.asia.buyers.models.orderimport.OrderBaseResponse
import zeemart.asia.buyers.models.orderimport.PlaceDraftOrdersResponse

/**
 * Created by RajPrudhviMarella on 11/11/2019.
 */
object OrderManagement {
    // api id - 21.1 Create draft orders based on the SKUs - (for Outlet)
    @JvmStatic
    fun createDraftOrdersBasedOnSKUs(
        context: Context?,
        requestBody: String?,
        getDraftOrderDataListener: CreateDraftOrderBasedOnSKUsListener,
        createDraftOrder: CreateDraftOrder
    ) {
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        val draftOutlet: Outlet = Outlet()
        draftOutlet.outletId = createDraftOrder.outletId
        outlet.add(draftOutlet)
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(createDraftOrder.outletId!!)
        var url: String? = ServiceConstant.ENDPOINT_CREATE_DRAFT_ORDER_FOR_SKU
        url = apiParamsHelper.getUrl(url)
        val getNotificationRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            url,
            requestBody,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    getDraftOrderDataListener.onSuccessResponse(response)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    getDraftOrderDataListener.onErrorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getNotificationRequest)
    }

    // api id - 21.2 Single Draft Order Update - (for Outlet)
    @JvmStatic
    fun singleDraftOrderUpdate(
        context: Context?, jsonRequestBody: String?, outletId: String?,
        outlets: List<Outlet>?, listener: SingleDraftOrderUpdateListener
    ) {
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletId!!)
        var Uri: String? = ServiceConstant.ENDPOINT_ORDERS_DRAFT
        Uri = apiParamsHelper.getUrl(Uri)
        val repeatOrderRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            Uri,
            jsonRequestBody,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (!StringHelper.isStringNullOrEmpty(response)) {
                        val orderBaseResponse: OrderBaseResponse? =
                            ZeemartBuyerApp.fromJson(response, OrderBaseResponse::class.java)
                        if ((orderBaseResponse != null) && (orderBaseResponse.data != null) && (orderBaseResponse.data!!
                                .data != null)
                        ) {
                            listener.onSuccessResponse(orderBaseResponse.data)
                        } else {
                            listener.onErrorResponse(VolleyError())
                        }
                    } else {
                        listener.onErrorResponse(VolleyError())
                    }
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(repeatOrderRequest)
    }

    // api id - 21.3 Place drafted orders after review
    @JvmStatic
    fun placeDraftedOrdersAfterReview(
        context: Context?,
        orderIds: List<String?>?,
        listener: PlaceDraftedOrdersAfterReviewListener
    ) {
        val placeDraftedOrdersAfterReviewRequestModel: PlaceDraftedOrdersAfterReviewRequestModel =
            PlaceDraftedOrdersAfterReviewRequestModel()
        placeDraftedOrdersAfterReviewRequestModel.outletId = SharedPref.defaultOutlet?.outletId
        placeDraftedOrdersAfterReviewRequestModel.orderIds = orderIds
        val jsonRequest: String = placeDraftedOrdersAfterReviewRequestModel.jsonRequest
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet?.outletId!!)
        var uri: String? = ServiceConstant.ENDPOINT_ORDERS_DRAFT_PLACE
        uri = apiParamsHelper.getUrl(uri)
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val repeatOrderRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            uri,
            jsonRequest,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (!StringHelper.isStringNullOrEmpty(response)) {
                        val draftPlacedResponse: PlaceDraftOrdersResponse? =
                            ZeemartBuyerApp.fromJson(response, PlaceDraftOrdersResponse::class.java)
                        if (draftPlacedResponse!!.data != null && draftPlacedResponse.data
                                ?.size!! > 0
                        ) {
                            listener.onSuccessResponse(draftPlacedResponse.data)
                        }
                    }
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(repeatOrderRequest)
    }

    fun createOrderFromDealForBuyers(
        context: Context?,
        jsonRequest: String?,
        dealNumber: String?,
        listener: NewDealOrderResponseListener
    ) {
        var uri: String? = ServiceConstant.ENDPOINT_PLACE_DEAL_ORDER
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet?.outletId!!)
        apiParamsHelper.setDealNumber(dealNumber!!)
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val createNewDealOrderRrequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            uri,
            jsonRequest,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    if (!StringHelper.isStringNullOrEmpty(response)) {
                        val responseData: NewCreateOrderResponse =
                            ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                response.toString(),
                                NewCreateOrderResponse::class.java
                            )
                        listener.onSuccessResponse(responseData)
                    }
                }

            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(createNewDealOrderRrequest)
    }

    fun createOrderFromEssentialsForBuyers(
        context: Context?,
        outletId: String?,
        jsonRequest: String?,
        essentialNumber: String?,
        listener: NewEssentialOrderResponseListener
    ) {
        var uri: String? = ServiceConstant.ENDPOINT_PLACE_ESSENTIAL_ORDER
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletId!!)
        apiParamsHelper.setEssentialsId(essentialNumber!!)
        uri = apiParamsHelper.getUrl(uri)
        var outlets: List<Outlet?>? = ArrayList<Outlet?>()
        outlets = SharedPref.allOutlets
        val createNewDealOrderRrequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            uri,
            jsonRequest,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    if (!StringHelper.isStringNullOrEmpty(response)) {
                        val responseData: NewCreateOrderResponse =
                            ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                response.toString(),
                                NewCreateOrderResponse::class.java
                            )
                        listener.onSuccessResponse(responseData)
                    }
                }

            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(createNewDealOrderRrequest)
    }

    @JvmStatic
    fun uploadTransactionImageToOrder(
        context: Context?,
        jsonRequest: String?,
        listener: uploadTransactionrResponseListener
    ) {
        var uri: String? = ServiceConstant.ENDPOINT_UPLOAD_TRANSACTION_IMAGE_ORDER
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet?.outletId!!)
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val createNewDealOrderRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            uri,
            jsonRequest,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (!StringHelper.isStringNullOrEmpty(response)) {
                        listener.onSuccessResponse(response)
                    }
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.onErrorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(createNewDealOrderRequest)
    }

    open interface CreateDraftOrderBasedOnSKUsListener {
        fun onSuccessResponse(response: String?)
        fun onErrorResponse(error: VolleyError?)
    }

    open interface SingleDraftOrderUpdateListener {
        fun onSuccessResponse(orderData: OrderBaseResponse.Data?)
        fun onErrorResponse(error: VolleyError?)
    }

    open interface PlaceDraftedOrdersAfterReviewListener {
        fun onSuccessResponse(placeDraftOrdersResponse: List<PlaceDraftOrdersResponse.OrdersData?>?)
        fun onErrorResponse(error: VolleyError?)
    }

    class PlaceDraftedOrdersAfterReviewRequestModel constructor() {
        @Expose
        var outletId: String? = null

        @SerializedName("orderIds")
        @Expose
        var orderIds: List<String?>? = null
        val jsonRequest: String
            get() {
                return ZeemartBuyerApp.gsonExposeExclusive.toJson(this)
            }
    }

    open interface NewDealOrderResponseListener {
        fun onSuccessResponse(newOrderResponse: NewCreateOrderResponse?)
        fun onErrorResponse(error: VolleyError?)
    }

    open interface NewEssentialOrderResponseListener {
        fun onSuccessResponse(newOrderResponse: NewCreateOrderResponse?)
        fun onErrorResponse(error: VolleyError?)
    }

    open interface uploadTransactionrResponseListener {
        fun onSuccessResponse(newOrderResponse: String?)
        fun onErrorResponse(error: VolleyError?)
    }
}