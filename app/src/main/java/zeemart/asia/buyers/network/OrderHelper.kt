package zeemart.asia.buyers.network

import android.content.Context
import android.view.View
import com.android.volley.Request
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import zeemart.asia.buyers.models.orderimport.OrderBaseResponse
import zeemart.asia.buyers.models.orderimport.OrdersWithPagination
import zeemart.asia.buyers.models.orderimport.RepeatOrderWithPagination
import zeemart.asia.buyers.models.orderimportimport.DraftOrdersBySKUPaginated
import zeemart.asia.buyers.models.orderimportimport.OrderSummary
import zeemart.asia.buyers.models.orderimportimport.OrderSummaryResponse
import zeemart.asia.buyers.models.orderimportimportimport.DealsProductsPaginated
import zeemart.asia.buyers.models.orderimportimportimport.SpclDealsBaseResponse
import zeemart.asia.buyers.modelsimport.AddOnOrderByOrderIdValidResponse
import zeemart.asia.buyers.modelsimport.DealsResponseForOnBoarding

/**
 * Created by saiful on 2/7/18.
 */
object OrderHelper {
    fun CancelOrderJson(order: Orders?, remarks: String?): String {
        val cancelOrderRequest = CancelOrderRequest()
        if (order != null) {
            cancelOrderRequest.orderId = order.orderId
            if (remarks != null) cancelOrderRequest.cancelledRemark = remarks
            return ZeemartBuyerApp.gsonExposeExclusive.toJson(cancelOrderRequest)
        }
        return ""
    }

    /**** Receive Order Helpers  */ // api id - 20.13 Received Orders
    fun receivedOrders(
        context: Context?,
        orderId: String?,
        outlet: Outlet,
        listener: RequestResponseListener,
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outlet.outletId!!)
        apiParamsHelper.setOrderId(orderId!!)
        var url: String = ServiceConstant.ENDPOINT_ORDER_RECEIVE_ORDER
        url = apiParamsHelper.getUrl(url)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val zeemartAPIRequestReceiveOrder = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            url,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val receivedOrderResponse: BaseResponse? =
                    ZeemartBuyerApp.fromJson(response, BaseResponse::class.java)
                if (receivedOrderResponse != null && receivedOrderResponse.status == ServiceConstant.STATUS_CODE_200_OK) {
                    listener.requestSuccessful()
                }
            }) { listener.requestError() }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(zeemartAPIRequestReceiveOrder)
    }

    // api id - 20.14 Repeat Order By supplier
    @JvmStatic
    fun getRepeatedOrdersBySupplier(
        context: Context?,
        outlet: Outlet,
        listener: RepeatOrderResponseListener,
    ) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val outletIds = arrayOfNulls<String>(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId
        }
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletIds(outletIds as Array<String>)
        val Uri: String = ServiceConstant.ENDPOINT_REPEAT_ORDERS
        val repeatOrderRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(Uri),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val repeatOrdersListBySupplier: RepeatOrderWithPagination? =
                    ZeemartBuyerApp.fromJson(response, RepeatOrderWithPagination::class.java)
                listener.onSuccessResponse(repeatOrdersListBySupplier)
            }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(repeatOrderRequest)
    }

    @JvmStatic
    fun returnDraftOrderForSupplier(
        context: Context?,
        supplierId: String,
        listener: DraftOrderResponseListener,
    ) {
        val orderStatus = arrayOf<String>(OrderStatus.DRAFT.statusName)
        val supplierList = arrayOf(supplierId)
        val retrieveOrderRequestParameters = ApiParamsHelper()
        retrieveOrderRequestParameters.setSortField(ApiParamsHelper.SortField.TIME_UPDATED)
        retrieveOrderRequestParameters.setSortOrder(ApiParamsHelper.SortOrder.DESC_CAP)
        retrieveOrderRequestParameters.setOrderStatus(orderStatus)
        retrieveOrderRequestParameters.setSupplierId(ApiParamsHelper.arraysToString(supplierList.toList()))
        //      fix for different outlet same supplier cannot create draft bug
        val outletId = arrayOf<String>(SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")!!)
        retrieveOrderRequestParameters.setOutletIds(outletId)
        retrieveOrderRequestParameters.setDefaultSixtyDaysPeriod(null)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        RetrieveOrders(context, object : RetrieveOrders.GetOrderDataListener {
            override fun onSuccessResponse(ordersData: OrdersWithPagination?) {
                if (ordersData != null && ordersData.data?.orders != null && ordersData.data!!.orders!!.size > 0
                ) {
                    val orders: List<Orders> = ordersData.data!!.orders!!
                    var draftOrder: Orders? = null
                    for (i in orders.indices) {
                        if (orders[i].isOrderByUser) {
                            draftOrder = orders[i]
                            break
                        }
                    }
                    listener.onSuccessResponse(draftOrder)
                } else {
                    listener.onSuccessResponse(null)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                listener.onErrorResponse(error)
            }
        }, outlets, retrieveOrderRequestParameters).orders
    }

    @JvmStatic
    fun returnDraftOrdersForOutlet(
        context: Context?,
        supplierIds: Array<String?>?,
        listener: OutletDraftOrdersListener,
    ) {
        val orderStatus = arrayOf<String>(OrderStatus.DRAFT.statusName)
        val retrieveOrderRequestParameters = ApiParamsHelper()
        retrieveOrderRequestParameters.setSortField(ApiParamsHelper.SortField.TIME_UPDATED)
        retrieveOrderRequestParameters.setSortOrder(ApiParamsHelper.SortOrder.DESC_CAP)
        retrieveOrderRequestParameters.setOrderStatus(orderStatus)
        retrieveOrderRequestParameters.setSupplierId(ApiParamsHelper.arraysToString(supplierIds?.toList() as List<String>))
        val outletId = arrayOf<String>(SharedPref.read(SharedPref.SELECTED_OUTLET_ID, "")!!)
        retrieveOrderRequestParameters.setOutletIds(outletId)
        retrieveOrderRequestParameters.setDefaultSixtyDaysPeriod(null)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        RetrieveOrders(context, object : RetrieveOrders.GetOrderDataListener {
            override fun onSuccessResponse(ordersData: OrdersWithPagination?) {
                if (ordersData != null && ordersData.data
                    ?.orders != null && ordersData.data!!.orders!!.size > 0
                ) {
                    val draftOrdersByUser: MutableList<Orders> = ArrayList<Orders>()
                    //add unique suppliers
                    val supplierIds: MutableSet<String> = HashSet()
                    for (i in 0 until ordersData.data!!.orders!!.size) {
                        if (ordersData.data!!.orders!!.get(i).isOrderByUser) {
                            val isSupplierAdded = ordersData.data!!.orders!!.get(i).supplier
                                ?.supplierId?.let {
                                    supplierIds.add(
                                        it
                                    )
                                }
                            //added check for preventing multiple draft from single supplier in the list
                            if (isSupplierAdded == true) {
                                draftOrdersByUser.add(ordersData.data!!.orders!!.get(i))
                            }
                        }
                    }
                    listener.onSuccessResponse(draftOrdersByUser)
                } else {
                    listener.onSuccessResponse(null)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                listener.onErrorResponse(error)
            }
        }, outlets, retrieveOrderRequestParameters).orders
    }

    //   api id - 35.9 Order Chart Data by Updated date
    @JvmStatic
    fun getChartsDataViewOrder(
        context: Context?,
        outlets: List<Outlet>?,
        apiParamsHelper: ApiParamsHelper,
        chartsDataListener: ChartsDataListener,
    ) {
        apiParamsHelper.setRemoveSquareBrackets(true)
        val Uri: String = apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_VIEW_ORDER_SUMMARY)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            Uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val chartsDataModel: ChartsDataModel =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson<ChartsDataModel>(
                        response,
                        ChartsDataModel::class.java
                    )
                chartsDataListener.onSuccessResponse(chartsDataModel)
            }) { error -> chartsDataListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    @JvmStatic
    fun getChartsDataViewOrderNew(context: Context?, orderSummaryListener: OrderSummaryListener) {
        val Uri: String = ServiceConstant.ENDPOINT_VIEW_ORDER_SUMMARY_NEW
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            Uri,
            null,
            CommonMethods.headerAllOutlets,
            { response ->
                val orderSummaryResponse: OrderSummaryResponse =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response,
                        OrderSummaryResponse::class.java
                    )
                orderSummaryListener.onSuccessResponse(orderSummaryResponse.data)
            }) { error -> orderSummaryListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    @JvmStatic
    fun checkExistingDraftThenDiscard(context: Context, supplierId: String, view: View?) {
        if (view != null) {
            CommonMethods.avoidMultipleClicks(view)
        }
        returnDraftOrderForSupplier(context, supplierId, object : DraftOrderResponseListener {
            override fun onSuccessResponse(order: Orders?) {
                if (order != null) {
                    //call the delete draft API
                    DeleteOrder(context, object : DeleteOrder.GetResponseStatusListener {
                        override fun onSuccessResponse(status: String?) {
                            // Draft Deleted No action Required
                        }

                        override fun onErrorResponse(error: VolleyError?) {
                            //Even Error also , No Action Required
                        }
                    }).deleteOrderData(order.orderId, order.outlet?.outletId)
                }
            }

            override fun onErrorResponse(error: VolleyError?) {}
        })
    }

    @JvmStatic
    fun getCartItemsCount(orders: List<Orders>?): Int {
        return if (orders != null && orders.size > 0) {
            var items = 0
            for (i in orders.indices) {
                items = items + orders[i].products?.size!!
            }
            items
        } else {
            0
        }
    }

    @JvmStatic
    fun getActiveDeals(
        context: Context?,
        dealNumber: String?,
        dealsResponseListener: DealsResponseListener,
    ) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val outletIds = arrayOfNulls<String>(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId
        }
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletIds(outletIds as Array<String>)
        if (!StringHelper.isStringNullOrEmpty(dealNumber)) {
            apiParamsHelper.setDealNumbers(dealNumber!!)
        }
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_ACTIVE_DEALS),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val dealsBaseResponse: DealsBaseResponse =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response,
                        DealsBaseResponse::class.java
                    )
                dealsResponseListener.onSuccessResponse(dealsBaseResponse)
            }) { error -> dealsResponseListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    @JvmStatic
    fun getActiveSpclDeals(
        context: Context?,
        dealNumber: String?,
        dealsResponseListener: SpclDealsResponseListener,
    ) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val outletIds = arrayOfNulls<String>(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId
        }
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletIds(outletIds as Array<String>)
        apiParamsHelper.setSortBy(ApiParamsHelper.SortField.DISPLAY_SEQUENCE)
        apiParamsHelper.setStatus(ApiParamsHelper.Status.ACTIVE)
        if (!StringHelper.isStringNullOrEmpty(dealNumber)) {
            apiParamsHelper.setDealNumbers(dealNumber!!)
        }
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_SPCL_DEALS),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val dealsBaseResponse: SpclDealsBaseResponse =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response,
                        SpclDealsBaseResponse::class.java
                    )
                dealsResponseListener.onSuccessResponse(dealsBaseResponse)
            }) { error -> dealsResponseListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    fun getActiveDealsForOnBoarding(
        context: Context?,
        dealsResponseListener: DealsOnBoardingResponseListener,
    ) {
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            ServiceConstant.ENDPOINT_RETRIEVE_DEALS,
            null,
            CommonMethods.getHeaderFromOutlets(null),
            { response ->
                val dealsBaseResponse: DealsResponseForOnBoarding =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response,
                        DealsResponseForOnBoarding::class.java
                    )
                dealsResponseListener.onSuccessResponse(dealsBaseResponse)
            }) { error -> dealsResponseListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    fun getActiveEssentialsDealsForOnBoarding(
        context: Context?,
        dealsResponseListener: DealsEssentialsOnBoardingResponseListener,
    ) {
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            ServiceConstant.ENDPOINT_RETRIEVE_DEALS_ESSENTIALS,
            null,
            CommonMethods.getHeaderFromOutlets(null),
            { response ->
                val dealsBaseResponse: SpclDealsBaseResponse =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response,
                        SpclDealsBaseResponse::class.java
                    )
                dealsResponseListener.onSuccessResponse(dealsBaseResponse)
            }) { error -> dealsResponseListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    fun getDealProducts(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        dealsResponseListener: DealsProductsListener,
    ) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val outletIds = arrayOfNulls<String>(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId
        }
        apiParamsHelper.setOutletIds(outletIds as Array<String>)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_DEAL_PRODUCTS),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val dealsBaseResponse: DealsProductsPaginated =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response,
                        DealsProductsPaginated::class.java
                    )
                dealsResponseListener.onSuccessResponse(dealsBaseResponse)
            }) { error -> dealsResponseListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    fun getDealOnBoardingProducts(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        dealsResponseListener: DealsProductsListener,
    ) {
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_RETRIEVE_DEAL_PRODUCTS),
            null,
            CommonMethods.getHeaderFromOutlets(null),
            { response ->
                val dealsBaseResponse: DealsProductsPaginated =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        response,
                        DealsProductsPaginated::class.java
                    )
                dealsResponseListener.onSuccessResponse(dealsBaseResponse)
            }) { error -> dealsResponseListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    @JvmStatic
    fun validateAddOnOrderBySupplierId(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        validAddOnOrderListener: ValidAddOnOrderListener,
    ) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_VALID_ADD_ON_ORDER_BY_SUPPLIER_ID),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val dealsBaseResponse: AddOnOrderValidResponse =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson<AddOnOrderValidResponse>(
                        response,
                        AddOnOrderValidResponse::class.java
                    )
                validAddOnOrderListener.onSuccessResponse(dealsBaseResponse)
            }) { error -> validAddOnOrderListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    fun validateAddOnOrderByOrderId(
        context: Context?,
        draftOrdersBySKUPaginated: DraftOrdersBySKUPaginated?,
        validAddOnOrderListener: ValidAddOnOrderByOrderIdListener,
    ) {
        if (draftOrdersBySKUPaginated?.data != null && draftOrdersBySKUPaginated.data!!.isNotEmpty()
        ) {
            val orderIds = arrayOfNulls<String>(draftOrdersBySKUPaginated.data!!.size)
            for (i in 0 until draftOrdersBySKUPaginated.data!!.size) {
                orderIds[i] = draftOrdersBySKUPaginated.data!!.get(i).orders?.orderId
            }
            if (orderIds.size == draftOrdersBySKUPaginated.data!!.size) {
                val outlets: MutableList<Outlet> = ArrayList<Outlet>()
                outlets.add(SharedPref.defaultOutlet!!)
                val apiParamsHelper = ApiParamsHelper()
                apiParamsHelper.setOrderId(orderIds)
                val zeemartAPIRequest = ZeemartAPIRequest(
                    context,
                    Request.Method.GET,
                    apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_VALID_ADD_ON_ORDER_BY_ORDER_ID),
                    null,
                    CommonMethods.getHeaderFromOutlets(outlets),
                    { response ->
                        if (!StringHelper.isStringNullOrEmpty(response)) {
                            val dealsBaseResponse: AddOnOrderByOrderIdValidResponse =
                                ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                    response,
                                    AddOnOrderByOrderIdValidResponse::class.java
                                )
                            for (i in 0 until draftOrdersBySKUPaginated.data!!.size) {
                                for (k in 0 until dealsBaseResponse.data!!.size) {
                                    if (draftOrdersBySKUPaginated.data!![i].orders
                                            ?.orderId
                                            .equals(dealsBaseResponse.data!![k].orderId)
                                    ) draftOrdersBySKUPaginated.data!![i].orders.addOns = (
                                        dealsBaseResponse.data!![k].IsAddOn()) as List<String>
                                }
                            }
                            validAddOnOrderListener.onSuccessResponse(draftOrdersBySKUPaginated)
                        }
                    }) { error -> validAddOnOrderListener.onErrorResponse(error) }
                VolleyRequest.Companion.getInstance(context)!!
                    .addToRequestQueue<String>(zeemartAPIRequest)
            }
        }
    }

    @JvmStatic
    fun validateAddOnOrderByOrderId(
        context: Context?,
        draftOrdersBySKUPaginated: OrderBaseResponse.Data?,
        validAddOnOrderListener: ValidAddOnOrderByOrderIdsListener,
    ) {
        if (draftOrdersBySKUPaginated?.data != null) {
            val orderIds: MutableList<String> = ArrayList()
            draftOrdersBySKUPaginated.data!!.orderId?.let { orderIds.add(it) }
            val outlets: MutableList<Outlet> = ArrayList<Outlet>()
            outlets.add(SharedPref.defaultOutlet!!)
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setOrderIds(orderIds)
            val zeemartAPIRequest = ZeemartAPIRequest(
                context,
                Request.Method.GET,
                apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_VALID_ADD_ON_ORDER_BY_ORDER_ID),
                null,
                CommonMethods.getHeaderFromOutlets(outlets),
                { response ->
                    if (!StringHelper.isStringNullOrEmpty(response)) {
                        val dealsBaseResponse: AddOnOrderByOrderIdValidResponse =
                            ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                response,
                                AddOnOrderByOrderIdValidResponse::class.java
                            )
                        draftOrdersBySKUPaginated.data!!.addOns = (dealsBaseResponse.data?.get(0)?.IsAddOn()) as List<String>
                        validAddOnOrderListener.onSuccessResponse(draftOrdersBySKUPaginated)
                    }
                }) { error -> validAddOnOrderListener.onErrorResponse(error) }
            VolleyRequest.Companion.getInstance(context)!!
                .addToRequestQueue<String>(zeemartAPIRequest)
        }
    }

    interface RequestResponseListener {
        fun requestSuccessful()
        fun requestError()
    }

    /**
     * Repeat order API
     */
    interface RepeatOrderResponseListener {
        fun onSuccessResponse(repeatOrdersListBySupplier: RepeatOrderWithPagination?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    //****** check if the user has an existing draft for the supplier outlet combination********//
    interface DraftOrderResponseListener {
        fun onSuccessResponse(order: Orders?)
        fun onErrorResponse(error: VolleyError?)
    }

    //****** check if the user has an existing draft for the supplier outlet combination********//
    interface OutletDraftOrdersListener {
        fun onSuccessResponse(orders: List<Orders>?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface ChartsDataListener {
        fun onSuccessResponse(chartsDataModel: ChartsDataModel?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface OrderSummaryListener {
        fun onSuccessResponse(orderSummary: OrderSummary?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface DealsResponseListener {
        fun onSuccessResponse(dealsBaseResponse: DealsBaseResponse?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface DealsOnBoardingResponseListener {
        fun onSuccessResponse(dealsBaseResponse: DealsResponseForOnBoarding?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface DealsEssentialsOnBoardingResponseListener {
        fun onSuccessResponse(dealsBaseResponse: SpclDealsBaseResponse?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface DealsProductsListener {
        fun onSuccessResponse(dealsBaseResponse: DealsProductsPaginated?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface ValidAddOnOrderListener {
        fun onSuccessResponse(addOnOrderValidResponse: AddOnOrderValidResponse?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface ValidAddOnOrderByOrderIdListener {
        fun onSuccessResponse(addOnOrderValidResponse: DraftOrdersBySKUPaginated?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface ValidAddOnOrderByOrderIdsListener {
        fun onSuccessResponse(draftOrdersBySKUPaginated: OrderBaseResponse.Data?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface SpclDealsResponseListener {
        fun onSuccessResponse(dealsBaseResponse: SpclDealsBaseResponse?)
        fun onErrorResponse(error: VolleyError?)
    }
}