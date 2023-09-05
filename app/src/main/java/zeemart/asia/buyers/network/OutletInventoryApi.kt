package zeemart.asia.buyers.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.modelsimport.InventoryTransferLocationModel

object OutletInventoryApi {
    //   api id - 16.2 Find All StockShelves by outletId
    @JvmStatic
    fun findAllStockShelvesByOutlet(context: Context?, listener: OutletStockShelvesListener) {
        val uri: String = ServiceConstant.ENDPOINT_INVENTORY_OUTLET_SHELVES
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    val allStockShelvesByOutlet: InventoryResponse.AllStockShelvesByOutlet? =
                        ZeemartBuyerApp.fromJson(response, InventoryResponse.AllStockShelvesByOutlet::class.java)
                    listener.onStockShelveListSuccess(allStockShelvesByOutlet)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.errorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    } //   api id - 16.2 Find All StockShelves by outletId

    @JvmStatic
    fun getAllTransferLocations(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        listener: OutletTransferListener
    ) {
        val uri: String =
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_INVENTORY_OUTLET_LIST_TRANSFER)
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        val outlet1: Outlet = SharedPref.defaultOutlet!!
        outlet.add(outlet1)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    val allStockShelvesByOutlet: InventoryTransferLocationModel? =
                        ZeemartBuyerApp.fromJson(
                            response,
                            InventoryTransferLocationModel::class.java
                        )
                    assert(allStockShelvesByOutlet != null)
                    listener.outletTransferSuccess(allStockShelvesByOutlet?.data)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.errorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    //   api id - 16.17 Retrieve Stockage Entries
    @JvmStatic
    fun getCountEntriesByOutlet(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        listener: OutletStockCountEntriesListener
    ) {
        var uri: String? = ServiceConstant.ENDPOINT_INVENTORY_OUTLET_STOCKAGES
        uri = apiParamsHelper.getUrl(uri)
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    val allStockCountEntries: InventoryResponse.StockCountEntriesByOutlet? =
                        ZeemartBuyerApp.fromJson(response, InventoryResponse.StockCountEntriesByOutlet::class.java)
                    listener.onStockShelveListSuccess(allStockCountEntries)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    Log.e("OutletInventoryAPI", error.message + "****")
                    listener.errorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    //   api id - 16.9 Retrieve Inventory Products
    @JvmStatic
    fun getProductForShelve(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        listener: ProductsByShelveDataListener
    ) {
        var uri: String? = ServiceConstant.ENDPOINT_INVENTORY_OUTLET_PRODUCTS
        uri = apiParamsHelper.getUrl(uri)
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    val productsByShelveId: InventoryResponse.ProductsByShelveId? =
                        ZeemartBuyerApp.fromJson(response, InventoryResponse.ProductsByShelveId::class.java)
                    listener.onStockShelveListSuccess(productsByShelveId)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    Log.e("OutletInventoryAPI", error.message + "****")
                    listener.errorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    //   api id - 16.15 Create Stock Count Entry
    fun createStockCountEntry(
        context: Context?,
        request: String?,
        listener: StockageEntryResponseListener
    ) {
        val uri: String = ServiceConstant.ENDPOINT_INVENTORY_OUTLET_SHELVE_STOCKCOUNT
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            uri,
            request,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (response != null) {
                        val stockCountEntryByOutlet: InventoryResponse.StockCountEntryByOutlet? =
                            ZeemartBuyerApp.fromJson(response, InventoryResponse.StockCountEntryByOutlet::class.java)
                        if (stockCountEntryByOutlet?.status === ServiceConstant.STATUS_CODE_200_OK) {
                            listener.onSuccessResponse(stockCountEntryByOutlet)
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
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    //   api id - 16.19 Delete Stockage
    @JvmStatic
    fun deleteStockCount(
        context: Context?,
        request: String?,
        listener: StockageEntryResponseListener
    ) {
        val uri: String = ServiceConstant.ENDPOINT_INVENTORY_OUTLET_STOCKAGE
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.DELETE,
            uri,
            request,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (response != null) {
                        val stockCountEntryByOutlet: InventoryResponse.StockCountEntryByOutlet? =
                            ZeemartBuyerApp.fromJson(response, InventoryResponse.StockCountEntryByOutlet::class.java)
                        listener.onSuccessResponse(stockCountEntryByOutlet)
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
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    //   api id - 16.18 Retrieve Inventory Stockage Entry by ID
    @JvmStatic
    fun retrieveInventoryStockageEntryById(
        context: Context?,
        stockageId: String?,
        listener: StockageEntryResponseListener
    ) {
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStockageId(stockageId!!)
        var uri: String? = ServiceConstant.ENDPOINT_INVENTORY_OUTLET_STOCKAGE
        uri = apiParamsHelper.getUrl(uri)
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (response != null) {
                        val stockCountEntryByOutlet: InventoryResponse.StockCountEntryByOutlet? =
                            ZeemartBuyerApp.fromJson(response, InventoryResponse.StockCountEntryByOutlet::class.java)
                        listener.onSuccessResponse(stockCountEntryByOutlet)
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
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    //   api id - 16.16 Create Adjustment Entry
    @JvmStatic
    fun createAdjustmentEntry(
        context: Context?,
        request: String?,
        listener: StockageEntryResponseListener
    ) {
        val uri: String = ServiceConstant.ENDPOINT_INVENTORY_OUTLET_SHELVE_ADJUSTMENT
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            uri,
            request,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (response != null) {
                        val stockCountEntryByOutlet: InventoryResponse.StockCountEntryByOutlet? =
                            ZeemartBuyerApp.fromJson(response, InventoryResponse.StockCountEntryByOutlet::class.java)
                        listener.onSuccessResponse(stockCountEntryByOutlet)
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
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    @JvmStatic
    fun createAmendmentEntry(
        context: Context?,
        request: String?,
        listener: StockageEntryResponseListener
    ) {
        val uri: String = ServiceConstant.ENDPOINT_INVENTORY_OUTLET_SHELVE_AMENDMENT
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.POST,
            uri,
            request,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (response != null) {
                        val stockCountEntryByOutlet: InventoryResponse.StockCountEntryByOutlet? =
                            ZeemartBuyerApp.fromJson(response, InventoryResponse.StockCountEntryByOutlet::class.java)
                        listener.onSuccessResponse(stockCountEntryByOutlet)
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
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    fun getDraftStockCount(
        context: Context?,
        shelveId: String?,
        request: String?,
        listener: StockageEntryResponseListener
    ) {
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setShelveId(shelveId!!)
        var uri: String = ServiceConstant.ENDPOINT_INVENTORY_DRAFT
        uri = apiParamsHelper.getUrl(uri)
        Log.d("/draft", "getDraftStockCount: " + uri)
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            request,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (response != null) {
                        val stockCountEntryByOutlet: InventoryResponse.StockCountEntryByOutlet? =
                            ZeemartBuyerApp.fromJson(response, InventoryResponse.StockCountEntryByOutlet::class.java)
                        listener.onSuccessResponse(stockCountEntryByOutlet)
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
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    fun deleteDraftStockCount(
        context: Context?,
        stockageId: String?,
        request: String?,
        listener: StockageEntryResponseListener
    ) {
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStockageId(stockageId!!)
        var uri: String? = ServiceConstant.ENDPOINT_INVENTORY_DRAFT
        uri = apiParamsHelper.getUrl(uri)
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.DELETE,
            uri,
            request,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (response != null) {
                        val stockCountEntryByOutlet: InventoryResponse.StockCountEntryByOutlet? =
                            ZeemartBuyerApp.fromJson(response, InventoryResponse.StockCountEntryByOutlet::class.java)
                        listener.onSuccessResponse(stockCountEntryByOutlet)
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
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    @JvmStatic
    fun getOnHandHistory(
        context: Context?,
        productId: String?,
        request: String?,
        listener: OnHandResponseListener
    ) {
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setProductId(productId!!)
        var uri: String? = ServiceConstant.ENDPOINT_INVENTORY_ON_HAND_HISTORY
        uri = apiParamsHelper.getUrl(uri)
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            request,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    if (response != null) {
                        val onHandHistoryData: InventoryResponse.OnHandHistoryData? =
                            ZeemartBuyerApp.fromJson(response, InventoryResponse.OnHandHistoryData::class.java)
                        listener.onSuccessResponse(onHandHistoryData)
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
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    open interface OutletStockShelvesListener {
        fun onStockShelveListSuccess(allStockShelveData: InventoryResponse.AllStockShelvesByOutlet?)
        fun errorResponse(error: VolleyError?)
    }

    open interface OutletTransferListener {
        fun outletTransferSuccess(outletsLists: List<InventoryTransferLocationModel.TransferOutlets?>?)
        fun errorResponse(error: VolleyError?)
    }

    open interface OutletStockCountEntriesListener {
        fun onStockShelveListSuccess(stockCountEntriesByOutletData: InventoryResponse.StockCountEntriesByOutlet?)
        fun errorResponse(error: VolleyError?)
    }

    open interface ProductsByShelveDataListener {
        fun onStockShelveListSuccess(productsByShelveId: InventoryResponse.ProductsByShelveId?)
        fun errorResponse(error: VolleyError?)
    }

    open interface StockageEntryResponseListener {
        fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?)
        fun onErrorResponse(error: VolleyError?)
    }

    open interface OnHandResponseListener {
        fun onSuccessResponse(data: InventoryResponse.OnHandHistoryData?)
        fun onErrorResponse(error: VolleyError?)
    }
}