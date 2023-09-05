package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.modelsimport.OrderSettingDeliveryPreferences
import zeemart.asia.buyers.modelsimport.OutletSuppliersResponseForOrderSettings

/**
 * Created by RajPrudhviMarella on 23/Aug/2021.
 */
object OrderSettingsApi {
    //   api id - 16.2 Find All StockShelves by outletId
    @JvmStatic
    fun getDeliveryDates(
        context: Context?,
        outletId: String?,
        supplierId: String?,
        listener: OrderSettingDeliveryDatesListener
    ) {
        val uri: String = ServiceConstant.ENDPOINT_INVENTORY_ORDER_SETTINGS_DELIVERY_DATES
        val outlets: List<Outlet> = SharedPref.allOutlets!!
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletId!!)
        apiParamsHelper.setSupplierId(supplierId!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(uri),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    val allStockShelvesByOutlet: OrderSettingDeliveryPreferences? =
                        ZeemartBuyerApp.fromJson(
                            response,
                            OrderSettingDeliveryPreferences::class.java
                        )
                    listener.onDeliveryDateListSuccess(allStockShelvesByOutlet)
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
    fun getAllOutletsSuppliers(context: Context?, listener: OrderSettingOutletsListener) {
        val uri: String = ServiceConstant.ENDPOINT_INVENTORY_RETRIEVE_OUTLETS_SUPPLIERS_FOR_REVIEW
        val outlets: List<Outlet> = SharedPref.allOutlets!!
        val outletIds: Array<String?> = arrayOfNulls(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId
        }
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletIds(outletIds as Array<String>)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(uri),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    val allStockShelvesByOutlet: OutletSuppliersResponseForOrderSettings? =
                        ZeemartBuyerApp.fromJson(
                            response,
                            OutletSuppliersResponseForOrderSettings::class.java
                        )
                    listener.onOutletSuccess(allStockShelvesByOutlet)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.errorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    //   api id - 16.2 Find All StockShelves by outletId
    @JvmStatic
    fun saveOrderSettings(
        context: Context?,
        outletLst: Outlet,
        supplierId: String?,
        requestBody: String?,
        listener: OrderSettingDeliveryDatesListener
    ) {
        val uri: String = ServiceConstant.ENDPOINT_INVENTORY_SAVE_ORDER_SETTINGS_DELIVERY_DATES
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(outletLst)
        val apiParamsHelper: ApiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(outletLst.outletId!!)
        apiParamsHelper.setSupplierId(supplierId!!)
        val zeemartAPIRequest: ZeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            apiParamsHelper.getUrl(uri),
            requestBody,
            CommonMethods.getHeaderFromOutlets(outlet),
            object : Response.Listener<String> {
                public override fun onResponse(response: String?) {
                    val allStockShelvesByOutlet: OrderSettingDeliveryPreferences? =
                        ZeemartBuyerApp.fromJson(
                            response,
                            OrderSettingDeliveryPreferences::class.java
                        )
                    listener.onDeliveryDateListSuccess(allStockShelvesByOutlet)
                }
            },
            object : Response.ErrorListener {
                public override fun onErrorResponse(error: VolleyError) {
                    listener.errorResponse(error)
                }
            })
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    open interface OrderSettingDeliveryDatesListener {
        fun onDeliveryDateListSuccess(allStockShelveData: OrderSettingDeliveryPreferences?)
        fun errorResponse(error: VolleyError?)
    }

    open interface OrderSettingOutletsListener {
        fun onOutletSuccess(allStockShelveData: OutletSuppliersResponseForOrderSettings?)
        fun errorResponse(error: VolleyError?)
    }
}