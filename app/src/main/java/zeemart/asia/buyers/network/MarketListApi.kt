package zeemart.asia.buyers.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.google.gson.JsonSyntaxException
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.ProductListBySupplierListner
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Favourite
import zeemart.asia.buyers.models.marketlist.ProductDetailsBySearch
import zeemart.asia.buyers.models.marketlist.ProductListBySupplier
import zeemart.asia.buyers.models.reportsimport.Products

/**
 * Created by ParulBhandari on 1/23/2018.
 */
object MarketListApi {
    //   api id - 15.1 Retrieve Supplier List V1(Outlet)
    @JvmStatic
    fun retrieveSupplierList(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        outlets: List<Outlet>?,
        listener: SupplierListResponseListener
    ) {
        var uri: String = ServiceConstant.ENDPOINT_SUPPLIER
        uri = apiParamsHelper.getUrl(uri)
        val getSupplierList = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                var supplierList: List<DetailSupplierDataModel?> =
                    ArrayList<DetailSupplierDataModel?>()
                val supplierPaginationData: DetailSupplierDataModel.SupplierPagination? =
                    ZeemartBuyerApp.fromJson<DetailSupplierDataModel.SupplierPagination>(
                        response,
                        DetailSupplierDataModel.SupplierPagination::class.java
                    )
                if (supplierPaginationData != null && supplierPaginationData.suppliers != null && supplierPaginationData.suppliers!!.size > 0) {
                    supplierList = supplierPaginationData.suppliers!!
                }
                listener.onSuccessResponse(supplierList)
            }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(getSupplierList)
    }

    @JvmStatic
    fun retrieveSupplierListNew(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        outlets: List<Outlet>?,
        listener: SupplierListResponseListener
    ) {
        var uri: String = ServiceConstant.ENDPOINT_SUPPLIER_NEW
        uri = apiParamsHelper.getUrl(uri)
        val getSupplierList = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                var supplierList: List<DetailSupplierDataModel?> =
                    ArrayList<DetailSupplierDataModel?>()
                val supplierPaginationData: DetailSupplierDataModel.SupplierResponseForDeliveryPreferences? =
                    ZeemartBuyerApp.fromJson<DetailSupplierDataModel.SupplierResponseForDeliveryPreferences>(
                        response,
                        DetailSupplierDataModel.SupplierResponseForDeliveryPreferences::class.java
                    )
                if (supplierPaginationData != null && supplierPaginationData.suppliers != null && supplierPaginationData.suppliers!!.size > 0) {
                    supplierList = supplierPaginationData.suppliers!!
                }
                listener.onSuccessResponse(supplierList)
            }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(getSupplierList)
    }

    //   api id - 14.7 Retrieve Market List (Outlet)
    @JvmStatic
    fun retrieveMarketListOutlet(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        outletId: String,
        listener: ProductListBySupplierListner
    ) {
        apiParamsHelper.setOutletId(outletId)
        Log.d(
            "****",
            "getProductList: " + apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_MARKET_LIST_BY_SUPPLIER)
        )
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val getProductListBySupplier = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_MARKET_LIST_BY_SUPPLIER),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val productListBySupplier: ProductListBySupplier? =
                    ZeemartBuyerApp.fromJson<ProductListBySupplier>(
                        response,
                        ProductListBySupplier::class.java
                    )
                listener.onSuccessResponse(productListBySupplier)
            }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getProductListBySupplier)
    }

    //   api id - 14.8 Retrieve Product list from Market List (Outlet)
    fun retrieveProductListOutlet(
        context: Context?,
        outletId: String,
        listener: ProductListListener
    ) {
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val getSKUlist = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            ServiceConstant.REPORT_SKU_SEARCH_LIST,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                try {
                    val allSKUs: Products.WithPagination? =
                        ZeemartBuyerApp.fromJson(response, Products.WithPagination::class.java)
                    val skuArray: Array<Products?>? =
                        allSKUs?.products?.let { arrayOfNulls<Products>(it.size) }
                    //listener.onSuccessResponse(skuArray)?)
                    listener.onSuccessResponse(skuArray)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(getSKUlist)
    }

    //   api id - 13.2 Search MarketList by Product Name - (for Outlet)
    @JvmStatic
    fun retrieveSearchProductListOutlet(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        outletId: String,
        listener: SearchProductListListener
    ) {
        var uri: String = ServiceConstant.ENDPOINT_SEARCH_BY_PRODUCT_NAME
        uri = apiParamsHelper.getUrl(uri)
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val getProductlist = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                try {
                    val productList: ProductDetailsBySearch.Response? =
                        ZeemartBuyerApp.fromJson<ProductDetailsBySearch.Response>(
                            response,
                            ProductDetailsBySearch.Response::class.java
                        )
                    listener.onSuccessResponse(productList)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(getProductlist)
    }

    //   api id - 14.11 Adding Favourites For Outlets (Outlet)
    @JvmStatic
    fun addingFavouritesOutlet(
        context: Context,
        favRequestObjectList: List<Favourite?>?,
        listener: RequestResponseListener
    ) {
        if (favRequestObjectList != null && favRequestObjectList.size > 0) {
            val jsonRequestBody: String =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(favRequestObjectList)
            val endpointUrl: String = ServiceConstant.ENDPOINT_ADD_FAVORITES
            val outlets: MutableList<Outlet> = ArrayList<Outlet>()
            outlets.add(SharedPref.defaultOutlet!!)
            val addFavRequest = ZeemartAPIRequest(
                context,
                Request.Method.PUT,
                endpointUrl,
                jsonRequestBody,
                CommonMethods.getHeaderFromOutlets(outlets),
                { response ->
                    if (response != null && response.contains(
                            context.getString(
                                R.string.txt_success
                            )
                        )
                    ) listener.requestSuccessful()
                }) { listener.requestError() }
            VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(addFavRequest)
        }
    }

    interface RequestResponseListener {
        fun requestSuccessful()
        fun requestError()
    }

    interface ProductListListener {
        fun onSuccessResponse(products: Array<Products?>?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    interface SearchProductListListener {
        fun onSuccessResponse(products: ProductDetailsBySearch.Response?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }
}