package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.EssentialsBaseResponse
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.orderimportimportimport.EssentialProductsPaginated
import zeemart.asia.buyers.modelsimport.EssentialsResponseForOnBoarding
import zeemart.asia.buyers.modelsimport.SearchEssentialAndDealsModel
import zeemart.asia.buyers.modelsimport.SelfOnBoardingSearchEssentialAndDealsModel

/**
 * Created by RajPrudhvi on 4/17/2018.
 */
object EssentialsApi {
    @JvmStatic
    fun getPaginatedEssentials(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        mListener: EssentialsResponseListener
    ) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val outletIds = arrayOfNulls<String>(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId
        }
        apiParamsHelper.setOutletIds(outletIds as Array<String>)
        val url: String = ServiceConstant.ENDPOINT_VIEW_ESSENTIALS
        val getEssentials = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(url),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val retrieveEssentials: EssentialsBaseResponse? =
                    ZeemartBuyerApp.fromJson<EssentialsBaseResponse>(
                        response,
                        EssentialsBaseResponse::class.java
                    )
                mListener.onSuccessResponse(retrieveEssentials)
            }) { error -> mListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(getEssentials)
    }

    @JvmStatic
    fun getOrderAgainPaginatedEssentials(context: Context?, mListener: EssentialsResponseListener) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val outletIds = arrayOfNulls<String>(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId
        }
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet!!.outletId!!)
        val url: String = ServiceConstant.ENDPOINT_VIEW_ORDER_AGAIN_ESSENTIALS
        val getEssentials = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(url),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val retrieveEssentials: EssentialsBaseResponse? =
                    ZeemartBuyerApp.fromJson<EssentialsBaseResponse>(
                        response,
                        EssentialsBaseResponse::class.java
                    )
                mListener.onSuccessResponse(retrieveEssentials)
            }) { error -> mListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(getEssentials)
    }

    fun getPaginatedEssentialsForOnBoarding(
        context: Context?,
        mListener: EssentialsResponseOnBoardingListener
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStatus(ApiParamsHelper.Status.ACTIVE)
        val url: String = ServiceConstant.ENDPOINT_RETRIEVE_ESSENTIALS
        val getEssentials = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(url),
            null,
            CommonMethods.getHeaderFromOutlets(null),
            { response ->
                val retrieveEssentials: EssentialsResponseForOnBoarding? =
                    ZeemartBuyerApp.fromJson(response, EssentialsResponseForOnBoarding::class.java)
                mListener.onSuccessResponse(retrieveEssentials)
            }) { error -> mListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(getEssentials)
    }

    fun getPaginatedEssentialsProducts(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        mListener: EssentialsProductsResponseListener
    ) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val outletIds = arrayOfNulls<String>(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId
        }
        apiParamsHelper.setOutletIds(outletIds as Array<String>)
        apiParamsHelper.setStatuses(ApiParamsHelper.Status.ACTIVE.value)
        val url: String = ServiceConstant.ENDPOINT_VIEW_ESSENTIALS_PRODUCTS
        val getEssentialsProducts = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(url),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val retrieveEssentialsProducts: EssentialProductsPaginated? =
                    ZeemartBuyerApp.fromJson(response, EssentialProductsPaginated::class.java)
                mListener.onSuccessResponse(retrieveEssentialsProducts)
            }) { error -> mListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getEssentialsProducts)
    }

    fun getPaginatedEssentialsOnBoardingProducts(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        mListener: EssentialsProductsOnBoardingResponseListener
    ) {
        val url: String = ServiceConstant.ENDPOINT_RETRIEVE_ESSENTIALS_PRODUCTS
        val getEssentialsProducts = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(url),
            null,
            CommonMethods.getHeaderFromOutlets(null),
            { response ->
                val retrieveEssentialsProducts: EssentialProductsPaginated? =
                    ZeemartBuyerApp.fromJson(response, EssentialProductsPaginated::class.java)
                mListener.onSuccessResponse(retrieveEssentialsProducts)
            }) { error -> mListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getEssentialsProducts)
    }

    @JvmStatic
    fun getSearchedEssentialAndDeals(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        mListener: EssentialsProductsSearchResponseListener
    ) {
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val outletIds = arrayOfNulls<String>(outlets.size)
        for (i in outlets.indices) {
            outletIds[i] = outlets[i].outletId
        }
        apiParamsHelper.setOutletIds(outletIds as Array<String>)
        val url: String = ServiceConstant.ENDPOINT_SEARCH_DEAL_ESSENTIALS
        val getEssentialsProducts = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(url),
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val retrieveEssentialsProducts: SearchEssentialAndDealsModel? =
                    ZeemartBuyerApp.fromJson(response, SearchEssentialAndDealsModel::class.java)
                mListener.onSuccessResponse(retrieveEssentialsProducts)
            }) { error -> mListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getEssentialsProducts)
    }

    fun getSearchedEssentialAndDealsForOnBoarding(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        mListener: SelfOnBoardingEssentialsProductsSearchResponseListener
    ) {
        val url: String = ServiceConstant.ENDPOINT_SEARCH_DEALS_ESSENTIALS_ON_BOARDING
        val getEssentialsProducts = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            apiParamsHelper.getUrl(url),
            null,
            CommonMethods.getHeaderFromOutlets(null),
            { response ->
                val retrieveEssentialsProducts: SelfOnBoardingSearchEssentialAndDealsModel? =
                    ZeemartBuyerApp.fromJson(
                        response,
                        SelfOnBoardingSearchEssentialAndDealsModel::class.java
                    )
                mListener.onSuccessResponse(retrieveEssentialsProducts)
            }) { error -> mListener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getEssentialsProducts)
    }

    @JvmStatic
    fun markEssentialsProductsAsFavourite(context: Context?, essentialProductId: String?) {
        val endpointUrl: String = ServiceConstant.ENDPOINT_MARK_ESSENTIALS_PRODUCT_AS_FAVOURITE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet?.outletId!!)
        apiParamsHelper.setEssentialProductId(essentialProductId!!)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val addFavRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            apiParamsHelper.getUrl(endpointUrl),
            "",
            CommonMethods.getHeaderFromOutlets(outlets),
            { }) { }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(addFavRequest)
    }

    @JvmStatic
    fun removeEssentialsProductsAsFavourite(context: Context?, essentialProductId: String?) {
        val endpointUrl: String = ServiceConstant.ENDPOINT_REMOVE_ESSENTIALS_PRODUCT_AS_FAVOURITE
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOutletId(SharedPref.defaultOutlet?.outletId!!)
        apiParamsHelper.setEssentialProductId(essentialProductId!!)
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(SharedPref.defaultOutlet!!)
        val removeFavRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            apiParamsHelper.getUrl(endpointUrl),
            "",
            CommonMethods.getHeaderFromOutlets(outlets),
            { }) { }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(removeFavRequest)
    }

    interface EssentialsResponseListener {
        fun onSuccessResponse(essentialsList: EssentialsBaseResponse?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface EssentialsResponseOnBoardingListener {
        fun onSuccessResponse(essentialsList: EssentialsResponseForOnBoarding?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface EssentialsProductsResponseListener {
        fun onSuccessResponse(essentialsList: EssentialProductsPaginated?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface EssentialsProductsOnBoardingResponseListener {
        fun onSuccessResponse(essentialsList: EssentialProductsPaginated?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface EssentialsProductsSearchResponseListener {
        fun onSuccessResponse(essentialsList: SearchEssentialAndDealsModel?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface SelfOnBoardingEssentialsProductsSearchResponseListener {
        fun onSuccessResponse(essentialsList: SelfOnBoardingSearchEssentialAndDealsModel?)
        fun onErrorResponse(error: VolleyError?)
    }
}